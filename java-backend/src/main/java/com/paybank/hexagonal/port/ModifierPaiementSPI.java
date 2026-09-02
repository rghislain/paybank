package com.paybank.hexagonal.port;

import java.util.UUID;

import com.paybank.hexagonal.domaine.MontantCentimes;
import com.paybank.hexagonal.domaine.TransactionPaiement;

public interface ModifierPaiementSPI {
	void modifier(UUID paiementId, MontantCentimes nouveauMontant);
}
