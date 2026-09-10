package com.paybank.hexagonal.adaptateur;

import com.paybank.hexagonal.domaine.MontantCentimes;
import com.paybank.hexagonal.port.PasserelleBancaireSPI;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Component
public class StripeMokbankAdaptateur implements PasserelleBancaireSPI {
	
	/*
	 * RestTemplate est une classe de Spring (Spring Web) qui sert à faire des appels HTTP vers d’autres services.
	   c’est un client HTTP Java.
	 */
    private final RestTemplate restTemplate;
  
    //L'URL devient configurable via application.properties. Si absente, elle garde sa valeur par défaut.
    @Value("${stripe.api.url:https://api.mokbank.internal/v1/charges}")
    private String stripeApiUrl;
    
    //vraie clé secrète de test Stripe (disponible sur le tableau de bord Stripe)
    private final String STRIPE_SECRET_KEY= "sk_test_51TBX0dEWZ4iSroOBwXh3i6MtrqnOWGKmGTal2SMcF9Guh3Jh0Hc6dIFCajDJ97ON3u5xnAH2hhaiePSjkPlBhYZZ00JgpiV3OU";
    
    public StripeMokbankAdaptateur(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public String executerTransactionBancaire(MontantCentimes montant, String tokenCarteMokbank) {
        try {
            //1. Configuration des Headers pour Stripe (Form URL Encoded + Clé Secrète)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(STRIPE_SECRET_KEY); //Injection automatique de "Bearer sk_test_..."

            //2. Construction du corps de la requête au format Form Data (MultiValueMap)
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("amount", String.valueOf(montant.getValeur())); //Montant en centimes
            requestBody.add("currency", "eur");
            requestBody.add("source", tokenCarteMokbank); //Le token commençant par 'tok_...' généré par Stripe Elements côté client
            requestBody.add("description", "Paiement sécurisé PayBank - Environnement Test");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);

            //3. Appel de l'API Stripe
            ResponseEntity<Map> reponse = restTemplate.postForEntity(stripeApiUrl, entity, Map.class); 
            //4. Analyse du statut de la réponse Stripe
            if (reponse.getStatusCode().is2xxSuccessful() && reponse.getBody() != null) {
                //Stripe renvoie un champ "paid" (booléen) ou un champ "status" égal à "succeeded"
                Boolean estPaye = (Boolean) reponse.getBody().get("paid");
                Object statut = (String) reponse.getBody().get("status");
                String paymentIntentId = (String) reponse.getBody().get("id");
                return paymentIntentId;
            }            
            throw new RuntimeException("Paiement stripe echoue");

        } catch (RestClientException e) {
            //En cas d'erreur de carte (fonds insuffisants, carte expirée...), Stripe lève une exception HTTP 402          
        	throw new RuntimeException("Erreur stripe:"+e.getMessage());
        }
    }
    
    @Override
    public boolean annulerPaiement(String paymentIntentId) {
    	//Cibler l'endpoint des remboursements (refunds)
        String url = "http://api.mokbank.internal:8080/v1/refunds";
    	
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(STRIPE_SECRET_KEY);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("payment_intent", paymentIntentId.toString());
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, Map.class);
            return true;

        } catch (Exception e) {
            System.err.println("Refund Stripe échoué : " + e.getMessage());
            return false;
        }
    }

}