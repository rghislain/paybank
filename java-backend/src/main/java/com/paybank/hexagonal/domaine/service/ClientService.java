package com.paybank.hexagonal.domaine.service;

import com.paybank.hexagonal.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.annotation.SecuredPermission;
import com.paybank.hexagonal.domaine.model.Client;
import com.paybank.hexagonal.domaine.model.Permission;
import com.paybank.hexagonal.domaine.model.Role;
import com.paybank.hexagonal.sortie.port.ClientSPI;
import com.paybank.hexagonal.sortie.port.StripeClientSPI;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ClientService {
	
	@Value("${STRIPE_API_KEY:sk_test_valeur_par_defaut}")
	private String stripeApiKey;

    private final ClientSPI clientSPI;
    private final StripeClientSPI stripeClientSPI;
    private final OperateurCourantService operateurCourantService;

    public ClientService(ClientSPI clientSPI, @Value("${stripe.api.key}") String apiKey, StripeClientSPI stripeClientSPI, OperateurCourantService operateurCourantService) {
        this.clientSPI = clientSPI;
		this.stripeClientSPI = stripeClientSPI;
		this.operateurCourantService = operateurCourantService;
        Stripe.apiKey = apiKey;
    }

    //CRÉER
    @MasquerDonneesSensibles
    public Client creerClient(String nom, String email, String role) throws StripeException {              
    	Client client = this.stripeClientSPI.creerClientStripe(nom, email, operateurCourantService.getRoleConnecte().toString());
        clientSPI.sauvegarder(client);
        return client;
    }

    //LIRE
    @MasquerDonneesSensibles
    public Client obtenirClient(UUID id, String role) {
        return clientSPI.trouverParId(id)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable pour l'id : " + id));
    }
 
    //MODIFIER
    @MasquerDonneesSensibles
    public void modifierClient(UUID id, String nouveauNom, String nouvelEmail, String role) throws StripeException {          
    	role = operateurCourantService.getRoleConnecte().toString();
    	Client clientExistant = obtenirClient(id, role);
        String stripeCustomerId = clientExistant.getStripeCustomerId();       
        if (stripeCustomerId == null || stripeCustomerId.trim().isEmpty()) {
            //--- CAS A : Aucun ID Stripe n'existe, on le crée sur Stripe ---
            CustomerCreateParams createParams = CustomerCreateParams.builder()
                    .setName(nouveauNom)
                    .setEmail(nouvelEmail)
                    .build();       
            stripeCustomerId = this.stripeClientSPI.creerClientStripe(nouveauNom, nouvelEmail, role).getStripeCustomerId(); //newStripeCustomer.getId();
        } else {
            //--- CAS B : Le client a déjà un ID Stripe, on met à jour ---
           this.stripeClientSPI.mettreAJourClientStripe(stripeCustomerId, nouveauNom, nouvelEmail, role);            
        }
        //On sauvegarde en base avec le stripeCustomerId (qu'il soit nouveau ou mis à jour)
        Client clientModifie = new Client(clientExistant.getId(), nouveauNom, nouvelEmail, stripeCustomerId);
        clientSPI.sauvegarder(clientModifie);
    }
    
    //SUPPRIMER
    @MasquerDonneesSensibles
    public void supprimerClient(UUID id, String role) throws StripeException {        
    	role = operateurCourantService.getRoleConnecte().toString();
    	Client clientExistant = obtenirClient(id, role);       
        //Sécurité : Vérifier si un ID Stripe existe avant d'appeler l'API Stripe
        String stripeId = clientExistant.getStripeCustomerId();
        if (stripeId != null && !stripeId.trim().isEmpty()) {
            try {
                stripeClientSPI.supprimerClientStripe(stripeId); //Customer.retrieve(stripeId);              
            } catch (Exception e) {
                //Si le client n'existe déjà plus sur Stripe, on logue l'avertissement mais on continue la suppression en BDD
                System.err.println("Avertissement Stripe : Impossible de supprimer le client sur Stripe (peut-être déjà supprimé) : " + e.getMessage());
            }
        }
        // Suppression en base locale
        clientSPI.supprimer(id);
    }
    
    public List<Client> listerTousLesClients() {
        return clientSPI.listerTousLesClients();
    }
    
}