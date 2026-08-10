package com.paybank.hexagonal.domaine;

import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Balance;
import com.stripe.param.CustomerCreateParams;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServiceStripe {

    public ServiceStripe(@Value("${stripe.api.key}") String apiKey) {
        Stripe.apiKey = apiKey; //clé secrète de test (sk_test_...)
    }

    // CRÉER CLIENT
    @MasquerDonneesSensibles
    public String cloudCreerClient(String nom, String email) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(nom)
                .setEmail(email)
                .build();
        Customer customer = Customer.create(params);
        return customer.getId(); // Renvoie le cus_XXXXX à stocker en BDD
    }

    // REQUÊTE SOLDE (BALANCE)
    @MasquerDonneesSensibles
    public Map<String, Object> recupererSolde() throws StripeException {
        Balance balance = Balance.retrieve();
        Map<String, Object> soldeMap = new HashMap<>();
        // Récupère le montant disponible dans la première devise trouvée (ex: EUR)
        soldeMap.put("disponible", balance.getAvailable().get(0).getAmount()); 
        soldeMap.put("devise", balance.getAvailable().get(0).getCurrency());
        return soldeMap;
    }
}