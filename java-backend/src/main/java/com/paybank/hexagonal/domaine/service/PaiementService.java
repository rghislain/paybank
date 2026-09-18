package com.paybank.hexagonal.domaine.service;

import com.paybank.hexagonal.annotation.AgainstBruteForce;
import com.paybank.hexagonal.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.annotation.RequireDroit;
import com.paybank.hexagonal.annotation.SecuredPermission;
import com.paybank.hexagonal.annotation.Securise;
import com.paybank.hexagonal.domaine.model.MatchResult;
import com.paybank.hexagonal.domaine.model.MontantCentimes;
import com.paybank.hexagonal.domaine.model.Transaction;
import com.paybank.hexagonal.domaine.model.TransactionDetail;
import com.paybank.hexagonal.domaine.model.TransactionPaiement;
import com.paybank.hexagonal.sortie.port.StripeMokankPaiementSPI;
import com.paybank.hexagonal.sortie.port.PaiementSPI;
import com.paybank.hexagonal.sortie.port.TransactionSPI;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaiementService {
    private final PaiementSPI paiementSPI;
    private final TransactionSPI transactionSPI;
    private final StripeMokankPaiementSPI stripeMokbankPaiementSPI;
    
    public PaiementService(PaiementSPI paiementSPI, TransactionSPI transactionSPI, StripeMokankPaiementSPI stripeMokankPaiementSPI) {
    	this.paiementSPI = paiementSPI;
		this.transactionSPI = transactionSPI;
		this.stripeMokbankPaiementSPI = stripeMokankPaiementSPI;
    } 
    
	//CRÉER UN INTENT DE PAIEMENT
	@MasquerDonneesSensibles
    @RequireDroit(ressource = "paiements")
    public Map<String, String> creerIntentionPaiement (UUID clientId, long montantCentimes) {
        Map<String, String> reponse = stripeMokbankPaiementSPI.creerIntentionPaiement(clientId, montantCentimes);
        return reponse;        
    }
    
    @MasquerDonneesSensibles
    public TransactionPaiement traiterPaiement(UUID compteClientId, int montantCentimes, String cleIdempotence, String tokenCarteMokbank) {
        //1. Vérification stricte de l'idempotence au niveau du domaine
        Optional<TransactionPaiement> transactionExistante = paiementSPI.chercherParCleIdempotence(cleIdempotence);
        if (transactionExistante.isPresent()) {
            return transactionExistante.get();
        }

        //2. Initialisation de la transaction en mode PENDING
        MontantCentimes montant = new MontantCentimes(montantCentimes);
        TransactionPaiement nouvelleTransaction = new TransactionPaiement(compteClientId, montant, cleIdempotence);
        paiementSPI.enregistrer(nouvelleTransaction);

        try {
	        //3. Appel de la passerelle bancaire tierce
	        String idInterneBanque = stripeMokbankPaiementSPI.executerTransactionBancaire(montant, tokenCarteMokbank);
	
	        if (!idInterneBanque.isBlank() && !idInterneBanque.isEmpty()) {
	            nouvelleTransaction.valider();
	            paiementSPI.enregistrer(nouvelleTransaction);
	            paiementSPI.mettreAJourIDPaymentIntent(nouvelleTransaction.getId(), idInterneBanque);
	        
	            //Enregistrement dans la table des transactions pour le Bilan
                BigDecimal montantDecimal = BigDecimal.valueOf(montantCentimes).movePointLeft(2);
	            
	            TransactionDetail nouvelleLigneBilan = new TransactionDetail(
	                    UUID.randomUUID(),  
	                    LocalDate.now(), 
	                    "Achat - Paiement Stripe validé (" + idInterneBanque + ")",
	                    montantDecimal,
	                    false,
	                    "En attente"
	                );
	                transactionSPI.enregistrer(compteClientId, nouvelleLigneBilan); // Nom de la méthode selon ton SPI
	        } 
	        else {
	            nouvelleTransaction.echouer();
	            paiementSPI.enregistrer(nouvelleTransaction);
	        }	 
        }
        catch(Exception e) {
        	nouvelleTransaction.echouer();
        	paiementSPI.enregistrer(nouvelleTransaction);
        }
        return nouvelleTransaction;
    }   
       
    //MODIFIER LE MONTANT D'UN PAIEMENT EN COURS   
    @MasquerDonneesSensibles
    public void modifierMontantPaiement(String stripePaymentIntentId, long nouveauMontantCentimes) {  	
        try {
			stripeMokbankPaiementSPI.modifierMontantPaiement(stripePaymentIntentId, nouveauMontantCentimes);
		} catch (StripeException e) {
			e.printStackTrace();
		}      
    }

    //SUPPRIMER / ANNULER UN PAIEMENT  
    @MasquerDonneesSensibles
    public void annulerPaiement(String stripePaymentIntentId) {   	
        stripeMokbankPaiementSPI.annulerIntentionPaiement(stripePaymentIntentId);
    }
      
    @MasquerDonneesSensibles
   	public void executerAnnulation(String clefIdempotence) {
   		//1. Récupérer le paiement en BDD
           var paiement = paiementSPI.chercherParCleIdempotence(clefIdempotence)
               .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable : " + clefIdempotence));
           //2. Règle métier : On ne peut annuler qu'un paiement au statut 'SUCCESS'
           if (!TransactionPaiement.StatutTransaction.SUCCESS.equals(paiement.getStatut())) {
               throw new IllegalStateException("Impossible d'annuler une transaction avec le statut : " + paiement.getStatut());
           }
           //3. Appel de la banque externe (Stripe / Mokbank)     
           boolean estAnnuleCoteBanque = stripeMokbankPaiementSPI.annulerPaiement(paiement.getStripe_payment_intent_id());
           if (estAnnuleCoteBanque) {
               //4. Mise à jour du statut final en BDD Supabase          
               paiementSPI.mettreAJourStatut(paiement.getId(), TransactionPaiement.StatutTransaction.REFUNDED);
           } else {
               //Optionnel : Gérer le cas où la banque refuse l'annulation
               throw new RuntimeException("Le partenaire bancaire a refusé l'annulation de la transaction.");
           }
   	}
       
    @MasquerDonneesSensibles
   	public void executerAnnulationSurIDPaymentIntent(String IDPaymentIntent) {
   		//1. Récupérer le paiement en BDD
           var paiement = paiementSPI.chercherParPaymentIntentId(IDPaymentIntent)
               .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable : " + IDPaymentIntent));
           //2. Règle métier : On ne peut annuler qu'un paiement au statut 'SUCCESS'
           if (!TransactionPaiement.StatutTransaction.SUCCESS.equals(paiement.getStatut())) {
               throw new IllegalStateException("Impossible d'annuler une transaction avec le statut : " + paiement.getStatut());
           }
           //3. Appel de la banque externe (Stripe / Mokbank)
           boolean estAnnuleCoteBanque = stripeMokbankPaiementSPI.annulerPaiement(paiement.getStripe_payment_intent_id());
           if (estAnnuleCoteBanque) {
               //4. Mise à jour du statut final en BDD Supabase       
               paiementSPI.mettreAJourStatut(paiement.getId(), TransactionPaiement.StatutTransaction.REFUNDED);
           } else {
               //Optionnel : Gérer le cas où la banque refuse l'annulation
               throw new RuntimeException("Le partenaire bancaire a refusé l'annulation de la transaction.");
           }
   	}	
    

    //CONSULTER LE SOLDE STRIPE (BALANCE)
    public Map<String, Object> obtenirSoldeCompte() throws StripeException {
        return stripeMokbankPaiementSPI.obtenirSoldeCompte();
    }
       
    @MasquerDonneesSensibles
    public void synchroniserStatutPaiement(String stripePaymentIntentId) throws StripeException {
        this.stripeMokbankPaiementSPI.synchroniserStatutPaiement(stripePaymentIntentId);    	
    }
      
    @MasquerDonneesSensibles
    public void effectuerRapprochement(UUID paiementId, String stripePaymentIntentId) throws StripeException {
        this.stripeMokbankPaiementSPI.effectuerRapprochement(paiementId, stripePaymentIntentId);
    }
        
    @MasquerDonneesSensibles
    public void annulerRapprochement(UUID paiementId) {
        this.stripeMokbankPaiementSPI.annulerRapprochement(paiementId);
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
        List<Transaction> banqueDisponibles = new ArrayList<>(banqueOriginale);

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

            if (meilleurScore >= 0.8 && meilleurMatch != null) {
                resultats.add(new MatchResult(tCompta, meilleurMatch, "MATCH", meilleurScore));
                banqueDisponibles.remove(meilleurMatch);
            } else {
                resultats.add(new MatchResult(tCompta, null, "MANQUANT", 0));
            }
        }

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
        List<Transaction> transactionsStripe = stripeMokbankPaiementSPI.chargerTransactionsStripe();
        return executerRapprochementGlobal(
                transactionsCompta,
                transactionsStripe
        );
    }
      
    public List<Transaction> chargerTransactionsDepuisSupabase() {
        // Le service appelle son port sans savoir comment c'est implémenté derrière
        return transactionSPI.chargerTransactionsSupabase();
    }
   
    @AgainstBruteForce(requetesMax = 3, secondes = 10)
    public boolean verifierStatutServeur() {
        return true;
    }    
}