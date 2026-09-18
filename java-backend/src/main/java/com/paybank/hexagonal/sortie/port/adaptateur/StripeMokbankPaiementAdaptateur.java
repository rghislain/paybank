package com.paybank.hexagonal.sortie.port.adaptateur;

import com.paybank.hexagonal.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.annotation.RequireDroit;
import com.paybank.hexagonal.domaine.model.MontantCentimes;
import com.paybank.hexagonal.domaine.model.Transaction;
import com.paybank.hexagonal.domaine.model.TransactionDetail;
import com.paybank.hexagonal.sortie.port.PaiementSPI;
import com.paybank.hexagonal.sortie.port.StripeMokankPaiementSPI;
import com.paybank.hexagonal.sortie.port.TransactionSPI;
import com.stripe.exception.StripeException;
import com.stripe.model.Balance;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Component
public class StripeMokbankPaiementAdaptateur implements StripeMokankPaiementSPI {
	
	/*
	 * RestTemplate est une classe de Spring (Spring Web) qui sert à faire des appels HTTP vers d’autres services.
	   c’est un client HTTP Java.
	 */
    private final RestTemplate restTemplate;
    private final PaiementSPI paiementSPI;
    private final TransactionSPI transactionSPI;
  
    //L'URL devient configurable via application.properties. Si absente, elle garde sa valeur par défaut.
    @Value("${stripe.api.url:https://api.mokbank.internal/v1/charges}")
    private String stripeApiUrl;
    
    //vraie clé secrète de test Stripe (disponible sur le tableau de bord Stripe)
    @Value("${stripe.api.key:sk_test_default}")
    private String STRIPE_SECRET_KEY;
    
    public StripeMokbankPaiementAdaptateur(RestTemplate restTemplate, PaiementSPI paiementSPI, TransactionSPI transactionSPI) {
        this.restTemplate = restTemplate;
        this.paiementSPI=paiementSPI;
		this.transactionSPI = transactionSPI;
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
	
    @Override
	@MasquerDonneesSensibles
    @RequireDroit(ressource = "paiements")
    public Map<String, String> creerIntentionPaiement(UUID clientId, long montantCentimes) {
    	//1. On génère un identifiant unique (qui servira aussi de clé d'idempotence)
        UUID paiementId = UUID.randomUUID();
        String idempotencyKey = "idemp_" + paiementId.toString();    	
    	//2. Création du PaymentIntent chez Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(montantCentimes)
                .setCurrency("eur")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();       
        //3. AJOUT DE LA CLÉ D'IDEMPOTENCE DANS LES OPTIONS DE REQUÊTE
        com.stripe.net.RequestOptions requestOptions = com.stripe.net.RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey)
                .build();
        
        PaymentIntent intent = null;
		try {
			intent = PaymentIntent.create(params, requestOptions);
		} catch (StripeException e) {
			e.printStackTrace();
		}        
        //4. Sauvegarde locale en BDD via votre SPI
        paiementSPI.creerPaiementLocal(paiementId, clientId, montantCentimes, intent.getId(), "PENDING", idempotencyKey);
        //paiementSPI.mettreAJourIDPaymentIntent(paiementId, paymentIntentId);
        //5. Envoi du client_secret au Frontend (Correction de .add() par .put())
        Map<String, String> reponse = new HashMap<>();
        reponse.put("clientSecret", intent.getClientSecret());
        reponse.put("paymentIntentId", intent.getId());
        reponse.put("paiementId", paiementId.toString());
        reponse.put("cleeIdempotence", idempotencyKey);
        reponse.put("clientID", clientId.toString());
        return reponse;
    }
    
    @Override
    @MasquerDonneesSensibles
    public void modifierMontantPaiement(String stripePaymentIntentId, long nouveauMontantCentimes) throws StripeException {  	
    	//1. Mise à jour chez Stripe
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);      
        //Règle de gestion : On ne modifie pas si déjà capturé ou annulé
        if (!"requires_payment_method".equals(intent.getStatus()) && !"requires_confirmation".equals(intent.getStatus())) {
            throw new IllegalStateException("Impossible de modifier un paiement déjà traité ou annulé.");
        }

        PaymentIntentUpdateParams params = PaymentIntentUpdateParams.builder()
                .setAmount(nouveauMontantCentimes)
                .build();
        intent.update(params);
        //2. Mise à jour dans ta BDD locale via le SPI
        paiementSPI.mettreAJourMontantLocal(stripePaymentIntentId, nouveauMontantCentimes);
    }
    
    @Override
    @MasquerDonneesSensibles
    public void annulerIntentionPaiement(String stripePaymentIntentId) {   	
    	//1. Annulation chez Stripe
        PaymentIntent intent = null;
		try {
			intent = PaymentIntent.retrieve(stripePaymentIntentId);
		} catch (StripeException e) {
			e.printStackTrace();
		}
        try {
			intent.cancel();
		} catch (StripeException e) {
			e.printStackTrace();
		}
        //2. Mise à jour du statut à 'CANCELLED' ou suppression en BDD locale
        paiementSPI.annulerPaiementLocal(stripePaymentIntentId);
    }
   
    public Map<String, Object> obtenirSoldeCompte() throws StripeException {
        Balance balance = Balance.retrieve();        
        //On extrait le premier solde disponible (ex: en EUR)
        long disponible = balance.getAvailable().get(0).getAmount();
        String devise = balance.getAvailable().get(0).getCurrency();
        Map<String, Object> solde = new HashMap<>();
        solde.put("disponible", disponible);
        solde.put("devise", devise);
        return solde;
    }
    
    @Override
    @MasquerDonneesSensibles
    public void synchroniserStatutPaiement(String stripePaymentIntentId) throws StripeException {
        //1. On demande à Stripe le statut réel de ce paiement
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);     
        //2. Si le paiement est réussi chez Stripe, on met à jour notre BDD locale
        if ("succeeded".equals(intent.getStatus())) {          
            paiementSPI.mettreAJourStatutLocal(intent.getId(), intent.getStatus());     
            //3. RÉCUPÉRATION DES INFOS POUR LE BILAN COMPTABLE
            //On récupère le paiement local pour avoir le clientId et le montant
            //Il faut s'assurer que persistancePaiementSPI possède une méthode chercherParStripeId
            Map<String, Object> paiementLocal = paiementSPI.chercherParStripeId(stripePaymentIntentId);           
            if (paiementLocal != null) {
                UUID clientId = (UUID) paiementLocal.get("client_id");
                long montantCentimes = ((Number) paiementLocal.get("montant_centimes")).longValue();                
                //Création de l'écriture comptable (Identique à ce qu'on fait dans ServicePaiementImplementation)
                BigDecimal montantDecimal = BigDecimal.valueOf(montantCentimes).movePointLeft(2);               
                TransactionDetail nouvelleLigneBilan = new TransactionDetail(
                    UUID.randomUUID(),  
                    LocalDate.now(), 
                    "Achat - Paiement Stripe validé (" + intent.getId() + ")",
                    montantDecimal,
                    false,
                    "En attente"
                );             
                //Enregistrement dans la table des transactions pour le Bilan
                transactionSPI.enregistrer(clientId, nouvelleLigneBilan);
            }
        }
    }
    
    
    @MasquerDonneesSensibles
    public void effectuerRapprochement(UUID paiementId, String stripePaymentIntentId) throws StripeException {
        //A. Récupérer le paiement local sous forme de Map
        Map<String, Object> paiementLocal = paiementSPI.chercherParId2(paiementId);
        if (paiementLocal == null || paiementLocal.isEmpty()) {
            throw new IllegalArgumentException("Paiement local introuvable.");
        }
        //B. Récupérer les informations réelles chez Stripe
        PaymentIntent intent = PaymentIntent.retrieve(stripePaymentIntentId);
        //C. Extraire le montant attendu (on gère le cast selon le type retourné par le driver JDBC)
        long montantAttendu = ((Number) paiementLocal.get("montant_centimes")).longValue();
        String stripeIdAttendu = (String) paiementLocal.get("stripe_payment_intent_id");
        //D. Détecter si le rapprochement est valide
        if (!estRapprochementValide(stripeIdAttendu, intent.getId(), montantAttendu, intent.getAmount(), intent.getStatus())) {
            paiementSPI.mettreAJourStatut(paiementId, "MISMATCH");
            throw new IllegalStateException("Rapprochement invalide : écart de montant ou statut incorrect.");
        }
        //E. Si valide, on enregistre
        paiementSPI.enregistrerRapprochement(paiementId, stripePaymentIntentId, "APPROUVÉ");
        paiementSPI.mettreAJourStatut(paiementId, "SUCCESS");
    }
    
    @MasquerDonneesSensibles
    public boolean estRapprochementValide(String idIntentAttendu, String idIntentReel, long montantAttenduCentimes, long montantReelCentimes, String stripeStatus) {
        //1. Vérification que l'ID Stripe correspond bien
        boolean identifiantsEgaux = idIntentAttendu.equals(idIntentReel);      
        //2. Vérification du montant
        boolean montantsEgaux = montantAttenduCentimes == montantReelCentimes;    
        //3. Vérification du succès
        boolean stripeValide = "succeeded".equals(stripeStatus);
        return identifiantsEgaux && montantsEgaux && stripeValide;
    }
    
    @MasquerDonneesSensibles
    public void annulerRapprochement(UUID paiementId) {
        Map<String, Object> paiementLocal = paiementSPI.chercherParId2(paiementId);
        if (paiementLocal == null || paiementLocal.isEmpty()) {
            throw new IllegalArgumentException("Paiement local introuvable.");
        }
        paiementSPI.supprimerRapprochement(paiementId);
        paiementSPI.mettreAJourStatut(paiementId, "PENDING");
    }
    
	@Override
	public String obtenirStatutPaiement(String paymentIntentId) {
		return null;
	}

	@Override
	public List<Transaction> chargerTransactionsStripe() throws StripeException {
		 //1. Paramètres pour lister les intentions de paiement (ex: les 20 derniers)
        com.stripe.param.PaymentIntentListParams params = 
            com.stripe.param.PaymentIntentListParams.builder()
                .setLimit(20L)
                .build();
        //2. Appel à l'API Stripe
        com.stripe.model.PaymentIntentCollection paymentIntents = com.stripe.model.PaymentIntent.list(params);
        List<Transaction> transactionsStripe = new ArrayList<>();
        //3. Conversion des PaymentIntents de Stripe en objets Transaction de votre domaine
        for (com.stripe.model.PaymentIntent intent : paymentIntents.getData()) {
            //On récupère la date de création (convertie de timestamp UNIX en LocalDate)
            LocalDate date = java.time.Instant.ofEpochSecond(intent.getCreated())
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
            //On crée la transaction avec les vraies données Stripe
            transactionsStripe.add(new Transaction(
                intent.getId(), //Identifiant Stripe (ex: pi_...)
                intent.getAmount(), //Montant en centimes
                date, //Date du paiement
                intent.getDescription() != null ? intent.getDescription() : "STRIPE-" + intent.getId(), //Référence
                "STRIPE" //Type
            ));
        }
        return transactionsStripe;
	}

	@Override
	public String intentionPaiement(UUID id, long montantCentimes) {
		return null;
	}

}