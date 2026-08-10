package com.paybank.hexagonal.domaine;

import com.paybank.hexagonal.domaine.*;
import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.main.*;
import com.paybank.hexagonal.ports.*;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ServicePaiementImplementation implements ExecutionPaiementUseCase {

    private final PersistancePaiementSPI persistancePaiementSPI;
    private final PasserelleBancaireSPI passerelleBancaireSPI;

    public ServicePaiementImplementation(PersistancePaiementSPI persistancePaiementSPI, PasserelleBancaireSPI passerelleBancaireSPI) {
        this.persistancePaiementSPI = persistancePaiementSPI;
        this.passerelleBancaireSPI = passerelleBancaireSPI;
    }

    @MasquerDonneesSensibles
    @Override
    public TransactionPaiement traiterPaiement(UUID compteClientId, int montantCentimes, String cleIdempotence, String tokenCarteMokbank) {
        // 1. Vérification stricte de l'idempotence au niveau du domaine
        Optional<TransactionPaiement> transactionExistante = persistancePaiementSPI.chercherParCleIdempotence(cleIdempotence);
        if (transactionExistante.isPresent()) {
            return transactionExistante.get();
        }

        // 2. Initialisation de la transaction en mode PENDING
        MontantCentimes montant = new MontantCentimes(montantCentimes);
        TransactionPaiement nouvelleTransaction = new TransactionPaiement(compteClientId, montant, cleIdempotence);
        persistancePaiementSPI.enregistrer(nouvelleTransaction);

        try {
	        // 3. Appel de la passerelle bancaire tierce
	        String idInterneBanque = passerelleBancaireSPI.executerTransactionBancaire(montant, tokenCarteMokbank);
	
	        if (!idInterneBanque.isBlank() && !idInterneBanque.isEmpty()) {
	            nouvelleTransaction.valider();
	            persistancePaiementSPI.enregistrer(nouvelleTransaction);
	            persistancePaiementSPI.mettreAJourIDPaymentIntent(nouvelleTransaction.getId(), idInterneBanque);
	        } 
	        else {
	            nouvelleTransaction.echouer();
	            persistancePaiementSPI.enregistrer(nouvelleTransaction);
	        }
	        // 4. Persistance de l'état final       
	        //return nouvelleTransaction; //persistancePaiementSPI.enregistrer(nouvelleTransaction);
        }
        catch(Exception e) {
        	nouvelleTransaction.echouer();
        	persistancePaiementSPI.enregistrer(nouvelleTransaction);
        }
        return nouvelleTransaction;
    }
    
    
    
}