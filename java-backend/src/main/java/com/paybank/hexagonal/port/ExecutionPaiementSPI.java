package com.paybank.hexagonal.port;

import java.util.UUID;
import com.paybank.hexagonal.domaine.TransactionPaiement;

public interface ExecutionPaiementSPI {
	TransactionPaiement traiterPaiement(UUID compteClientId, int montantCentimes, String cleIdempotence, String tokenCarteMokbank);
}
