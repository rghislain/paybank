package com.paybank.hexagonal.ports;

import java.util.UUID;

import com.paybank.hexagonal.domaine.TransactionPaiement;

public interface ExecutionPaiementUseCase {
	TransactionPaiement traiterPaiement(UUID compteClientId, int montantCentimes, String cleIdempotence, String tokenCarteMokbank);
}
