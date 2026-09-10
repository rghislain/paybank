package com.paybank.hexagonal.domaine.service;

import java.util.UUID;
import org.springframework.transaction.event.TransactionPhase;
import com.paybank.hexagonal.domaine.TransactionPaiement;
import com.paybank.hexagonal.domaine.TransactionPaiement.StatutTransaction;
import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.port.AnnulerPaiementSPI;
import com.paybank.hexagonal.port.PasserelleBancaireSPI;
import com.paybank.hexagonal.port.PersistancePaiementSPI;

public class AnnulationService implements AnnulerPaiementSPI {
	private final PasserelleBancaireSPI passerelleBancaireSPI;
    private final PersistancePaiementSPI persistancePaiementSPI;

    public AnnulationService(PasserelleBancaireSPI passerelleBancaireSPI, PersistancePaiementSPI persistancePaiementSPI) {
        this.passerelleBancaireSPI = passerelleBancaireSPI;
        this.persistancePaiementSPI = persistancePaiementSPI;
    }
	
    @MasquerDonneesSensibles
	@Override
	public void executerAnnulation(String clefIdempotence) {
		//1. Récupérer le paiement en BDD
        var paiement = persistancePaiementSPI.chercherParCleIdempotence(clefIdempotence)
            .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable : " + clefIdempotence));
        //2. Règle métier : On ne peut annuler qu'un paiement au statut 'SUCCESS'
        if (!TransactionPaiement.StatutTransaction.SUCCESS.equals(paiement.getStatut())) {
            throw new IllegalStateException("Impossible d'annuler une transaction avec le statut : " + paiement.getStatut());
        }
        //3. Appel de la banque externe (Stripe / Mokbank)     
        boolean estAnnuleCoteBanque = passerelleBancaireSPI.annulerPaiement(paiement.getStripe_payment_intent_id());
        if (estAnnuleCoteBanque) {
            //4. Mise à jour du statut final en BDD Supabase          
            persistancePaiementSPI.mettreAJourStatut(paiement.getId(), TransactionPaiement.StatutTransaction.REFUNDED);
        } else {
            //Optionnel : Gérer le cas où la banque refuse l'annulation
            throw new RuntimeException("Le partenaire bancaire a refusé l'annulation de la transaction.");
        }
	}
    
    @MasquerDonneesSensibles
	@Override
	public void executerAnnulationSurIDPaymentIntent(String IDPaymentIntent) {
		//1. Récupérer le paiement en BDD
        var paiement = persistancePaiementSPI.chercherParPaymentIntentId(IDPaymentIntent)
            .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable : " + IDPaymentIntent));
        //2. Règle métier : On ne peut annuler qu'un paiement au statut 'SUCCESS'
        if (!TransactionPaiement.StatutTransaction.SUCCESS.equals(paiement.getStatut())) {
            throw new IllegalStateException("Impossible d'annuler une transaction avec le statut : " + paiement.getStatut());
        }
        //3. Appel de la banque externe (Stripe / Mokbank)
        boolean estAnnuleCoteBanque = passerelleBancaireSPI.annulerPaiement(paiement.getStripe_payment_intent_id());
        if (estAnnuleCoteBanque) {
            //4. Mise à jour du statut final en BDD Supabase       
            persistancePaiementSPI.mettreAJourStatut(paiement.getId(), TransactionPaiement.StatutTransaction.REFUNDED);
        } else {
            //Optionnel : Gérer le cas où la banque refuse l'annulation
            throw new RuntimeException("Le partenaire bancaire a refusé l'annulation de la transaction.");
        }
	}	
}