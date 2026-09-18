package com.paybank.hexagonal.sortie.port;

import com.paybank.hexagonal.domaine.model.Client;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;

public interface StripeClientSPI {
	 Client creerClientStripe(String nom, String email, String role) throws StripeException;
	 Customer chercherClientStripe(String stripeCustomerId, String nom, String email, String role) throws StripeException;
	 Customer mettreAJourClientStripe(String stripeCustomerId, String nom, String email, String role)
			throws StripeException;
	 void supprimerClientStripe(String stripeCustomerId) throws StripeException;
}
