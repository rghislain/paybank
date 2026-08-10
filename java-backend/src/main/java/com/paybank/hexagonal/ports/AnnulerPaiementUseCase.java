package com.paybank.hexagonal.ports;

import java.util.UUID;

public interface AnnulerPaiementUseCase {
	void executerAnnulation(String clefIdempotence);
	void executerAnnulationSurIDPaymentIntent(String IDPaymentIntent);
}
