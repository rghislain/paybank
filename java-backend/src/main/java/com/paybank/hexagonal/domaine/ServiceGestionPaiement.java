package com.paybank.hexagonal.domaine;

import com.paybank.hexagonal.domaine.annotation.AgainstBruteForce;
import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.domaine.annotation.Securise;
import com.paybank.hexagonal.domaine.controleurs.SecurityInterceptor;
import com.paybank.hexagonal.ports.PasserelleBancaireSPI;
import com.paybank.hexagonal.ports.PersistancePaiementSPI;
import com.stripe.exception.StripeException;
import com.stripe.model.Balance;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;

import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ServiceGestionPaiement {

    private final PersistancePaiementSPI persistancePaiementSPI;

    public ServiceGestionPaiement(PersistancePaiementSPI persistancePaiementSPI) {
        this.persistancePaiementSPI = persistancePaiementSPI;
    }

    // C - CRÉER UN INTENT DE PAIEMENT
    @MasquerDonneesSensibles
    public Map<String, String> creerIntentionPaiement(UUID clientId, long montantCentimes) throws StripeException {
    	String role = SecurityInterceptor.getContextRole();
    	if (!"EMPLOYE".equals(role)) {
    	    throw new IllegalArgumentException("Interdit par le domaine : seuls les managers peuvent gérer les articles");
    	}
    	
    	// 1. On génère un identifiant unique (qui servira aussi de clé d'idempotence)
        UUID paiementId = UUID.randomUUID();
        String idempotencyKey = "idemp_" + paiementId.toString();
    	
    	// 2. Création du PaymentIntent chez Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(montantCentimes)
                .setCurrency("eur")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();
        
        // 3. ➕ AJOUT DE LA CLÉ D'IDEMPOTENCE DANS LES OPTIONS DE REQUÊTE
        com.stripe.net.RequestOptions requestOptions = com.stripe.net.RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey)
                .build();
        
        PaymentIntent intent = PaymentIntent.create(params, requestOptions);
        
        // 4. Sauvegarde locale en BDD via votre SPI
        persistancePaiementSPI.creerPaiementLocal(paiementId, clientId, montantCentimes, intent.getId(), "PENDING", idempotencyKey);

        // 2. Sauvegarde locale en BDD via ton SPI existant
        // On génère un ID local pour notre table de paiements
        //UUID paiementId = UUID.randomUUID();
        //persistancePaiementSPI.creerPaiementLocal(paiementId, clientId, montantCentimes, intent.getId(), "PENDING");

        // 3. Envoi du client_secret au Frontend (Correction de .add() par .put())
        Map<String, String> reponse = new HashMap<>();
        reponse.put("clientSecret", intent.getClientSecret());
        reponse.put("paymentIntentId", intent.getId());
        reponse.put("paiementId", paiementId.toString());
        return reponse;
    }

    // U - MODIFIER LE MONTANT D'UN PAIEMENT EN COURS
    @MasquerDonneesSensibles
    public void modifierMontantPaiement(String stripePaymentIntentId, long nouveauMontantCentimes) throws StripeException {
    	String role = SecurityInterceptor.getContextRole();
    	if (!"EMPLOYE".equals(role)) {
    	    throw new IllegalArgumentException("Interdit par le domaine : seuls les managers peuvent gérer les articles");
    	}
    	// 1. Mise à jour chez Stripe
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);
        
        // Règle de gestion : On ne modifie pas si déjà capturé ou annulé
        if (!"requires_payment_method".equals(intent.getStatus()) && !"requires_confirmation".equals(intent.getStatus())) {
            throw new IllegalStateException("Impossible de modifier un paiement déjà traité ou annulé.");
        }

        PaymentIntentUpdateParams params = PaymentIntentUpdateParams.builder()
                .setAmount(nouveauMontantCentimes)
                .build();
        intent.update(params);

        // 2. Mise à jour dans ta BDD locale via le SPI
        persistancePaiementSPI.mettreAJourMontantLocal(stripePaymentIntentId, nouveauMontantCentimes);
    }

    // D - SUPPRIMER / ANNULER UN PAIEMENT
    @MasquerDonneesSensibles
    public void annulerPaiement(String stripePaymentIntentId) throws StripeException {
    	String role = SecurityInterceptor.getContextRole();
    	if (!"EMPLOYE".equals(role)) {
    	    throw new IllegalArgumentException("Interdit par le domaine : seuls les managers peuvent gérer les articles");
    	}
    	// 1. Annulation chez Stripe
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);
        intent.cancel();

        // 2. Mise à jour du statut à 'CANCELLED' ou suppression en BDD locale
        persistancePaiementSPI.annulerPaiementLocal(stripePaymentIntentId);
    }

    // R - CONSULTER LE SOLDE STRIPE (BALANCE)
    public Map<String, Object> obtenirSoldeCompte() throws StripeException {
        Balance balance = Balance.retrieve();
        
        // On extrait le premier solde disponible (ex: en EUR)
        long disponible = balance.getAvailable().get(0).getAmount();
        String devise = balance.getAvailable().get(0).getCurrency();

        Map<String, Object> solde = new HashMap<>();
        solde.put("disponible", disponible);
        solde.put("devise", devise);
        return solde;
    }
    
    @MasquerDonneesSensibles
    public void synchroniserStatutPaiement(String stripePaymentIntentId) throws StripeException {
        // 1. On demande à Stripe le statut réel de ce paiement
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);
        
        // 2. Si le paiement est réussi chez Stripe, on met à jour notre BDD locale
        if ("succeeded".equals(intent.getStatus())) {
            // Note : Utilisez la méthode de mise à jour appropriée de votre persistancePaiementSPI 
            // Si vous n'avez pas de méthode specifique, vous pouvez appeler une méthode générique de statut :
            persistancePaiementSPI.mettreAJourStatutLocal(intent.getId(), intent.getStatus());
        }
    }
    
    @MasquerDonneesSensibles
    public void effectuerRapprochement(UUID paiementId, String stripePaymentIntentId) throws StripeException {
        // A. Récupérer le paiement local sous forme de Map
        Map<String, Object> paiementLocal = persistancePaiementSPI.chercherParId2(paiementId);
        if (paiementLocal == null || paiementLocal.isEmpty()) {
            throw new IllegalArgumentException("Paiement local introuvable.");
        }

        // B. Récupérer les informations réelles chez Stripe
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);

        // C. Extraire le montant attendu (on gère le cast selon le type retourné par le driver JDBC)
        long montantAttendu = ((Number) paiementLocal.get("montant_centimes")).longValue();
        String stripeIdAttendu = (String) paiementLocal.get("stripe_payment_intent_id");
        // D. Détecter si le rapprochement est valide
        if (!estRapprochementValide(stripeIdAttendu, intent.getId(), montantAttendu, intent.getAmount(), intent.getStatus())) {
            persistancePaiementSPI.mettreAJourStatut(paiementId, "MISMATCH");
            throw new IllegalStateException("Rapprochement invalide : écart de montant ou statut incorrect.");
        }

        // E. Si valide, on enregistre
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

    /*
    public boolean estRapprochementValide(long montantAttenduCentimes, long montantReelCentimes, String stripeStatus) {
        boolean montantsEgaux = montantAttenduCentimes == montantReelCentimes;
        boolean stripeValide = "succeeded".equals(stripeStatus);
        return montantsEgaux && stripeValide;
    }
    */
    
    @MasquerDonneesSensibles
    public boolean estRapprochementValide(String idIntentAttendu, String idIntentReel, long montantAttenduCentimes, long montantReelCentimes, String stripeStatus) {
        // 1. Vérification que l'ID Stripe correspond bien
        boolean identifiantsEgaux = idIntentAttendu.equals(idIntentReel);
        
        // 2. Vérification du montant
        boolean montantsEgaux = montantAttenduCentimes == montantReelCentimes;
        
        // 3. Vérification du succès
        boolean stripeValide = "succeeded".equals(stripeStatus);

        return identifiantsEgaux && montantsEgaux && stripeValide;
    }
    
    /**
     * Algorithme principal de rapprochement global
     */
    @MasquerDonneesSensibles
    public List<MatchResult> executerRapprochementGlobal(List<Transaction> compta, List<Transaction> banqueOriginale) {
        List<MatchResult> resultats = new ArrayList<>();
        // Copie pour pouvoir supprimer les éléments matchés sans modifier la liste d'origine
        List<Transaction> banqueDisponibles = new ArrayList<>(banqueOriginale);

        // Boucle sur la comptabilité interne
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

            // Validation du seuil à 80% (0.8)
            if (meilleurScore >= 0.8 && meilleurMatch != null) {
                resultats.add(new MatchResult(tCompta, meilleurMatch, "MATCH", meilleurScore));
                banqueDisponibles.remove(meilleurMatch); // Évite le double matching
            } else {
                resultats.add(new MatchResult(tCompta, null, "MANQUANT", 0));
            }
        }

        // Étape 4 : Transactions bancaires restantes = Écarts inconnus (Oublis/Erreurs)
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

        // 1. Montant identique (50%)
        if (Math.abs(compta.montantCentimes()) == Math.abs(banque.montantCentimes())) {
            score += 0.50;
        }

        // 2. Date proche ± 1 à 3 jours (20%)
        long joursEcart = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(compta.date(), banque.date()));
        if (joursEcart <= 3) {
            score += 0.20;
        }

        // 3. Référence similaire (20%)
        if (compta.reference() != null && banque.reference() != null) {
            String refCompta = compta.reference().toLowerCase().trim();
            String refBanque = banque.reference().toLowerCase().trim();
            
            if (refCompta.equals(refBanque) || refCompta.contains(refBanque) || refBanque.contains(refCompta)) {
                score += 0.20;
            }
        }

        // 4. Type cohérent (10%)
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
    
    //@Securise
    @Securise(roles = {"ADMIN"})
    public List<MatchResult> executerRapprochementDepuisSources() throws com.stripe.exception.StripeException {
        // 1. Récupération des transactions de la comptabilité interne (Supabase)
        List<Transaction> transactionsCompta = chargerTransactionsDepuisSupabase();

        // 2. Récupération des transactions réelles du compte bancaire (Stripe)
        List<Transaction> transactionsStripe = chargerTransactionsDepuisStripe();

        // 3. Exécution de l'algorithme de rapprochement hexagonal
        return executerRapprochementGlobal(transactionsCompta, transactionsStripe);
    }
    
    // --- COUPLAGE AUX LOGS ET SIMULATIONS DE DONNÉES ENTRANTES ---
    @MasquerDonneesSensibles
    private List<Transaction> chargerTransactionsDepuisSupabase() {
        return List.of(
            new Transaction("C1", 4500, LocalDate.now(), "REF-STRIPE-45", "FACTURE"),
            new Transaction("C2", 2000, LocalDate.now().minusDays(1), "REF-STRIPE-20", "FACTURE")
        );
    }

    @MasquerDonneesSensibles
    private List<Transaction> chargerTransactionsDepuisStripe() throws com.stripe.exception.StripeException {
        return List.of(
            new Transaction("ST-1", 4500, LocalDate.now(), "REF-STRIPE-45", "STRIPE"),
            new Transaction("ST-2", 8900, LocalDate.now(), "VIR-MYSTERE", "STRIPE")
        );
    }
    
    @AgainstBruteForce(requetesMax = 3, secondes = 10)
    public boolean verifierStatutServeur() {
        return true;
    }
    
    @Before("execution(* com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement.createUser(..)) && args(utilisateur)")
    public void verifierInjection(Utilisateur utilisateur) {
        if (utilisateur.getNom().contains("<script>") || utilisateur.getEmail().contains("';")) {
            throw new IllegalArgumentException("Données suspectes détectées !");
        }
    }

    // Uniquement pour la compilation si StripeMethod n'est pas encore totalement setup
    //public Map<String, String> creerIntentionPaiement(java.util.UUID c, long m) throws com.stripe.exception.StripeException { return Map.of(); }
    //public void modifierMontantPaiement(String id, long m) throws com.stripe.exception.StripeException {}
    //public void annulerPaiement(String id) throws com.stripe.exception.StripeException {}
    //public Map<String, Object> obtenirSoldeCompte() throws com.stripe.exception.StripeException { return Map.of(); }
    //public void synchroniserStatutPaiement(String id) throws com.stripe.exception.StripeException {}
    
}