package com.paybank.hexagonal.ports;

import java.util.UUID;

import com.paybank.hexagonal.domaine.MontantCentimes;
import com.paybank.hexagonal.domaine.TransactionPaiement;

public interface ModifierPaiementUseCase {
	void modifier(UUID paiementId, MontantCentimes nouveauMontant);
}
