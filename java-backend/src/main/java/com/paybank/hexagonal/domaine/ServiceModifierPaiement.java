package com.paybank.hexagonal.domaine;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.ports.ModifierPaiementUseCase;
import com.paybank.hexagonal.ports.PersistancePaiementSPI;

public class ServiceModifierPaiement implements ModifierPaiementUseCase {
	private final PersistancePaiementSPI persistancePaiementSPI;

    public ServiceModifierPaiement(PersistancePaiementSPI persistancePaiementSPI) {
        this.persistancePaiementSPI = persistancePaiementSPI;
    }

    @MasquerDonneesSensibles
    public void modifier(UUID paiementId, MontantCentimes nouveauMontant) {
    	TransactionPaiement paiement = persistancePaiementSPI.chercherParId(paiementId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable"));
        paiement.modifierMontant(nouveauMontant);
        persistancePaiementSPI.modifierPaiement(paiement);
    }

}