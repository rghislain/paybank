package com.paybank.hexagonal.domaine.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.paybank.hexagonal.domaine.MontantCentimes;
import com.paybank.hexagonal.domaine.TransactionPaiement;
import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.port.ModifierPaiementSPI;
import com.paybank.hexagonal.port.PersistancePaiementSPI;

public class ModifierPaiementService implements ModifierPaiementSPI {
	private final PersistancePaiementSPI persistancePaiementSPI;

    public ModifierPaiementService(PersistancePaiementSPI persistancePaiementSPI) {
        this.persistancePaiementSPI = persistancePaiementSPI;
    }

    @MasquerDonneesSensibles
    //@CheckDroit(ressource = "paiements")
    public void modifier(UUID paiementId, MontantCentimes nouveauMontant) {
    	TransactionPaiement paiement = persistancePaiementSPI.chercherParId(paiementId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable"));
        paiement.modifierMontant(nouveauMontant);
        persistancePaiementSPI.modifierPaiement(paiement);
    }

}