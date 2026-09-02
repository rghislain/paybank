package com.paybank.hexagonal.port;

import java.util.UUID;

public interface AnnulerPaiementSPI {
	void executerAnnulation(String clefIdempotence);
	void executerAnnulationSurIDPaymentIntent(String IDPaymentIntent);
}
