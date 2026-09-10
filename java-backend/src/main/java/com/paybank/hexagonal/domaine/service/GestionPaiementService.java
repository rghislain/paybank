package com.paybank.hexagonal.domaine.service;

import com.paybank.hexagonal.configuration.SecurityInterceptor;
import com.paybank.hexagonal.domaine.MatchResult;
import com.paybank.hexagonal.domaine.Transaction;
import com.paybank.hexagonal.domaine.TransactionDetail;
import com.paybank.hexagonal.domaine.annotation.AgainstBruteForce;
import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.domaine.annotation.RequireDroit;
import com.paybank.hexagonal.domaine.annotation.SecuredPermission;
import com.paybank.hexagonal.domaine.annotation.Securise;
import com.paybank.hexagonal.port.PasserelleBancaireSPI;
import com.paybank.hexagonal.port.PersistancePaiementSPI;
import com.paybank.hexagonal.port.TransactionRepositorySPI;
import com.stripe.exception.StripeException;
import com.stripe.model.Balance;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GestionPaiementService {
    private final PersistancePaiementSPI persistancePaiementSPI;
    private final TransactionRepositorySPI transactionRepositorySPI;

    public GestionPaiementService(PersistancePaiementSPI persistancePaiementSPI, TransactionRepositorySPI transactionRepositorySPI) {
        this.persistancePaiementSPI = persistancePaiementSPI;
		this.transactionRepositorySPI = transactionRepositorySPI;
    }
    
    //CRÉER UN INTENT DE PAIEMENT
    @MasquerDonneesSensibles
    @RequireDroit(ressource = "paiements")
    public Map<String, String> creerIntentionPaiement(UUID clientId, long montantCentimes) throws StripeException {
    	//1. On génère un identifiant unique (qui servira aussi de clé d'idempotence)
        UUID paiementId = UUID.randomUUID();
        String idempotencyKey = "idemp_" + paiementId.toString();    	
    	//2. Création du PaymentIntent chez Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(montantCentimes)
                .setCurrency("eur")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();       
        //3. AJOUT DE LA CLÉ D'IDEMPOTENCE DANS LES OPTIONS DE REQUÊTE
        com.stripe.net.RequestOptions requestOptions = com.stripe.net.RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey)
                .build();
        
        PaymentIntent intent = PaymentIntent.create(params, requestOptions);
        
        //4. Sauvegarde locale en BDD via votre SPI
        persistancePaiementSPI.creerPaiementLocal(paiementId, clientId, montantCentimes, intent.getId(), "PENDING", idempotencyKey);

        //5. Envoi du client_secret au Frontend (Correction de .add() par .put())
        Map<String, String> reponse = new HashMap<>();
        reponse.put("clientSecret", intent.getClientSecret());
        reponse.put("paymentIntentId", intent.getId());
        reponse.put("paiementId", paiementId.toString());
        return reponse;
    }

    //MODIFIER LE MONTANT D'UN PAIEMENT EN COURS
    @MasquerDonneesSensibles
    public void modifierMontantPaiement(String stripePaymentIntentId, long nouveauMontantCentimes) throws StripeException {  	
    	//1. Mise à jour chez Stripe
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);
        
        //Règle de gestion : On ne modifie pas si déjà capturé ou annulé
        if (!"requires_payment_method".equals(intent.getStatus()) && !"requires_confirmation".equals(intent.getStatus())) {
            throw new IllegalStateException("Impossible de modifier un paiement déjà traité ou annulé.");
        }

        PaymentIntentUpdateParams params = PaymentIntentUpdateParams.builder()
                .setAmount(nouveauMontantCentimes)
                .build();
        intent.update(params);

        //2. Mise à jour dans ta BDD locale via le SPI
        persistancePaiementSPI.mettreAJourMontantLocal(stripePaymentIntentId, nouveauMontantCentimes);
    }

    //SUPPRIMER / ANNULER UN PAIEMENT
    @MasquerDonneesSensibles
    public void annulerPaiement(String stripePaymentIntentId) throws StripeException {   	
    	//1. Annulation chez Stripe
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);
        intent.cancel();

        //2. Mise à jour du statut à 'CANCELLED' ou suppression en BDD locale
        persistancePaiementSPI.annulerPaiementLocal(stripePaymentIntentId);
    }

    //CONSULTER LE SOLDE STRIPE (BALANCE)
    public Map<String, Object> obtenirSoldeCompte() throws StripeException {
        Balance balance = Balance.retrieve();
        
        //On extrait le premier solde disponible (ex: en EUR)
        long disponible = balance.getAvailable().get(0).getAmount();
        String devise = balance.getAvailable().get(0).getCurrency();

        Map<String, Object> solde = new HashMap<>();
        solde.put("disponible", disponible);
        solde.put("devise", devise);
        return solde;
    }
    
    @MasquerDonneesSensibles
    public void synchroniserStatutPaiement(String stripePaymentIntentId) throws StripeException {
        //1. On demande à Stripe le statut réel de ce paiement
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);
        
        //2. Si le paiement est réussi chez Stripe, on met à jour notre BDD locale
        if ("succeeded".equals(intent.getStatus())) {          
            persistancePaiementSPI.mettreAJourStatutLocal(intent.getId(), intent.getStatus());     
            //3. RÉCUPÉRATION DES INFOS POUR LE BILAN COMPTABLE
            //On récupère le paiement local pour avoir le clientId et le montant
            //Il faut s'assurer que persistancePaiementSPI possède une méthode chercherParStripeId
            Map<String, Object> paiementLocal = persistancePaiementSPI.chercherParStripeId(stripePaymentIntentId);           
            if (paiementLocal != null) {
                UUID clientId = (UUID) paiementLocal.get("client_id");
                long montantCentimes = ((Number) paiementLocal.get("montant_centimes")).longValue();                
                //Création de l'écriture comptable (Identique à ce qu'on fait dans ServicePaiementImplementation)
                BigDecimal montantDecimal = BigDecimal.valueOf(montantCentimes).movePointLeft(2);               
                TransactionDetail nouvelleLigneBilan = new TransactionDetail(
                    UUID.randomUUID(),  
                    LocalDate.now(), 
                    "Achat - Paiement Stripe validé (" + intent.getId() + ")",
                    montantDecimal,
                    false,
                    "En attente"
                );             
                //Enregistrement dans la table des transactions pour le Bilan
                transactionRepositorySPI.enregistrer(clientId, nouvelleLigneBilan);
            }
        }
    }
    
    @MasquerDonneesSensibles
    public void effectuerRapprochement(UUID paiementId, String stripePaymentIntentId) throws StripeException {
        //A. Récupérer le paiement local sous forme de Map
        Map<String, Object> paiementLocal = persistancePaiementSPI.chercherParId2(paiementId);
        if (paiementLocal == null || paiementLocal.isEmpty()) {
            throw new IllegalArgumentException("Paiement local introuvable.");
        }
        //B. Récupérer les informations réelles chez Stripe
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);
        //C. Extraire le montant attendu (on gère le cast selon le type retourné par le driver JDBC)
        long montantAttendu = ((Number) paiementLocal.get("montant_centimes")).longValue();
        String stripeIdAttendu = (String) paiementLocal.get("stripe_payment_intent_id");
        //D. Détecter si le rapprochement est valide
        if (!estRapprochementValide(stripeIdAttendu, intent.getId(), montantAttendu, intent.getAmount(), intent.getStatus())) {
            persistancePaiementSPI.mettreAJourStatut(paiementId, "MISMATCH");
            throw new IllegalStateException("Rapprochement invalide : écart de montant ou statut incorrect.");
        }
        //E. Si valide, on enregistre
        persistancePaiementSPI.enregistrerRapprochement(paiementId, stripePaymentIntentId, "APPROUVÉ");
        persistancePaiementSPI.mettreAJourStatut(paiementId, "SUCCESS");
    }
    
    @MasquerDonneesSensibles
    public void annulerRapprochement(UUID paiementId) {
        Map<String, Object> paiementLocal = persistancePaiementSPI.chercherParId2(paiementId);
        if (paiementLocal == null || paiementLocal.isEmpty()) {
            throw new IllegalArgumentException("Paiement local introuvable.");
        }
        persistancePaiementSPI.supprimerRapprochement(paiementId);
        persistancePaiementSPI.mettreAJourStatut(paiementId, "PENDING");
    }
    
    @MasquerDonneesSensibles
    public boolean estRapprochementValide(String idIntentAttendu, String idIntentReel, long montantAttenduCentimes, long montantReelCentimes, String stripeStatus) {
        //1. Vérification que l'ID Stripe correspond bien
        boolean identifiantsEgaux = idIntentAttendu.equals(idIntentReel);
        
        //2. Vérification du montant
        boolean montantsEgaux = montantAttenduCentimes == montantReelCentimes;
        
        //3. Vérification du succès
        boolean stripeValide = "succeeded".equals(stripeStatus);

        return identifiantsEgaux && montantsEgaux && stripeValide;
    }
    
    /**
     * Algorithme principal de rapprochement global
     */
    @MasquerDonneesSensibles
    public List<MatchResult> executerRapprochementGlobal(List<Transaction> compta, List<Transaction> banqueOriginale) {
        List<MatchResult> resultats = new ArrayList<>();
        //Copie pour pouvoir supprimer les éléments matchés sans modifier la liste d'origine
        List<Transaction> banqueDisponibles = new ArrayList<>(banqueOriginale);

        //Boucle sur la comptabilité interne
        for (Transaction tCompta : compta) {
            Transaction meilleurMatch = null;
            double meilleurScore = 0;

            for (Transaction tBanque : banqueDisponibles) {
                double score = calculerScore(tCompta, tBanque);
                if (score > meilleurScore) {
                    meilleurScore = score;
                    meilleurMatch = tBanque;
                }
            }

            //Validation du seuil à 80% (0.8)
            if (meilleurScore >= 0.8 && meilleurMatch != null) {
                resultats.add(new MatchResult(tCompta, meilleurMatch, "MATCH", meilleurScore));
                banqueDisponibles.remove(meilleurMatch); // Évite le double matching
            } else {
                resultats.add(new MatchResult(tCompta, null, "MANQUANT", 0));
            }
        }

        //Transactions bancaires restantes = Écarts inconnus (Oublis/Erreurs)
        for (Transaction tBanque : banqueDisponibles) {
            resultats.add(new MatchResult(null, tBanque, "INCONNU", 0));
        }

        return resultats;
    }

    /**
     * Étape 3 : Calcul du Score de correspondance pondéré
     */
    public double calculerScore(Transaction compta, Transaction banque) {
        double score = 0.0;
        //1. Montant identique (50%)
        if (Math.abs(compta.montantCentimes()) == Math.abs(banque.montantCentimes())) {
            score += 0.50;
        }

        //2. Date proche ± 1 à 3 jours (20%)
        long joursEcart = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(compta.date(), banque.date()));
        if (joursEcart <= 3) {
            score += 0.20;
        }

        //3. Référence similaire (20%)
        if (compta.reference() != null && banque.reference() != null) {
            String refCompta = compta.reference().toLowerCase().trim();
            String refBanque = banque.reference().toLowerCase().trim();
            
            if (refCompta.equals(refBanque) || refCompta.contains(refBanque) || refBanque.contains(refCompta)) {
                score += 0.20;
            }
        }

        //4. Type cohérent (10%)
        if (estTypeCoherent(compta.type(), banque.type())) {
            score += 0.10;
        }

        return score;
    }
    
    /**
     * Vérification de la cohérence des types (Flux croisés)
     */
    private boolean estTypeCoherent(String typeCompta, String typeBanque) {
        if ("FACTURE".equals(typeCompta) && ("ENCAISSEMENT".equals(typeBanque) || "STRIPE".equals(typeBanque) || "VIREMENT".equals(typeBanque))) {
            return true;
        }
        if ("PAIEMENT_ATTENDU".equals(typeCompta) && "STRIPE".equals(typeBanque)) {
            return true;
        }
        return typeCompta.equalsIgnoreCase(typeBanque); // Par défaut si même nomenclature
    }
    
    /**
     * Vérification finale de l'égalité des soldes (Objectif Principal)
     */    
    @MasquerDonneesSensibles
    public boolean verifierEgaliteSoldes(long soldeComptableCentimes, long soldeBancaireCentimes) {
        return soldeComptableCentimes == soldeBancaireCentimes;
    }  
      
    @MasquerDonneesSensibles
    //Défense en profondeur : cette annotation protège aussi les appels directs à cette
    //méthode (ex. tests, futurs appels internes) qui ne passeraient pas par
    //PaiementControleur.executerRapprochementBancaire() (déjà protégé de façon identique).
    @RequireDroit(action = "creer", ressource = "rapports_financiers")
    public List<MatchResult> executerRapprochementDepuisSources()
            throws com.stripe.exception.StripeException {

        List<Transaction> transactionsCompta =
                chargerTransactionsDepuisSupabase();

        List<Transaction> transactionsStripe =
                chargerTransactionsDepuisStripe();

        return executerRapprochementGlobal(
                transactionsCompta,
                transactionsStripe
        );
    }
      
    public List<Transaction> chargerTransactionsDepuisSupabase() {
        // Le service appelle son port sans savoir comment c'est implémenté derrière
        return transactionRepositorySPI.chargerTransactionsSupabase();
    }
    
    @MasquerDonneesSensibles
    private List<Transaction> chargerTransactionsDepuisStripe() throws com.stripe.exception.StripeException {
        //1. Paramètres pour lister les intentions de paiement (ex: les 20 derniers)
        com.stripe.param.PaymentIntentListParams params = 
            com.stripe.param.PaymentIntentListParams.builder()
                .setLimit(20L)
                .build();

        //2. Appel à l'API Stripe
        com.stripe.model.PaymentIntentCollection paymentIntents = com.stripe.model.PaymentIntent.list(params);

        List<Transaction> transactionsStripe = new ArrayList<>();

        //3. Conversion des PaymentIntents de Stripe en objets Transaction de votre domaine
        for (com.stripe.model.PaymentIntent intent : paymentIntents.getData()) {
            //On récupère la date de création (convertie de timestamp UNIX en LocalDate)
            LocalDate date = java.time.Instant.ofEpochSecond(intent.getCreated())
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

            //On crée la transaction avec les vraies données Stripe
            transactionsStripe.add(new Transaction(
                intent.getId(), //Identifiant Stripe (ex: pi_...)
                intent.getAmount(), //Montant en centimes
                date, //Date du paiement
                intent.getDescription() != null ? intent.getDescription() : "STRIPE-" + intent.getId(), //Référence
                "STRIPE" //Type
            ));
        }
        return transactionsStripe;
    }
    
    @AgainstBruteForce(requetesMax = 3, secondes = 10)
    public boolean verifierStatutServeur() {
        return true;
    }    
}