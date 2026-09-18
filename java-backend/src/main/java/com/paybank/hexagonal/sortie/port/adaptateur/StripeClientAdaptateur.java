package com.paybank.hexagonal.sortie.port.adaptateur;

import org.springframework.stereotype.Component;

import com.paybank.hexagonal.domaine.model.Client;
import com.paybank.hexagonal.sortie.port.StripeClientSPI;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;

@Component
public class StripeClientAdaptateur implements StripeClientSPI {

	@Override
	public Client creerClientStripe(String nom, String email, String role) throws StripeException {
		CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(nom)
                .setEmail(email)
                .setBusinessName(role)
                .build();
        Customer stripeCustomer = Customer.create(params);
        Client client = new Client(null, nom, email, stripeCustomer.getId());
        return client;
	}

	@Override
	public Customer mettreAJourClientStripe(String stripeCustomerId, String nom, String email, String role) throws StripeException {
		 Customer stripeCustomer = Customer.retrieve(stripeCustomerId);
         CustomerUpdateParams updateParams = CustomerUpdateParams.builder()
                 .setName(nom)
                 .setEmail(email)
                 .setBusinessName(role)
                 .build();
         stripeCustomer.update(updateParams);
         return stripeCustomer;
	}

	@Override
	public void supprimerClientStripe(String stripeCustomerId) throws StripeException {
		Customer customerStripe = Customer.retrieve(stripeCustomerId);
		if (customerStripe != null) {
            customerStripe.delete();
        }
	}

	@Override
	public Customer chercherClientStripe(String stripeCustomerId, String nom, String email, String role)
			throws StripeException {
		return Customer.retrieve(stripeCustomerId);		
	}
}