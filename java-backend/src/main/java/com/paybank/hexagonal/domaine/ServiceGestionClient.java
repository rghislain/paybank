package com.paybank.hexagonal.domaine;

import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.domaine.controleurs.SecurityInterceptor;
import com.paybank.hexagonal.ports.ClientSPI;
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
public class ServiceGestionClient {

    private final ClientSPI clientSPI;

    public ServiceGestionClient(ClientSPI clientSPI, @Value("${stripe.api.key}") String apiKey) {
        this.clientSPI = clientSPI;
        Stripe.apiKey = apiKey;
    }
    
    // Méthode utilitaire interne de vérification des permissions
    @MasquerDonneesSensibles
    private void verifierPermission(Permission permissionRequise) {
        String headerRole = null;
        try {
            headerRole = ((jakarta.servlet.http.HttpServletRequest) 
                ((org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                .getRequest()).getHeader("X-Auth-Role");
        } catch (Exception e) {
            headerRole = SecurityInterceptor.getContextRole();
        }

        System.out.println("====== DOMAINE - LOG SÉCURITÉ - RÔLE REÇU : [" + headerRole + "] ======");

        if (headerRole == null || headerRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Action refusée par le Domaine : Aucun rôle fourni !");
        }

        try {
            Role domaineRole = Role.valueOf(headerRole.trim().toUpperCase());
            if (!domaineRole.hasPermission(permissionRequise)) {
                throw new IllegalArgumentException("Action refusée par le Domaine : Droits insuffisants pour ce rôle !");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Action refusée par le Domaine : Rôle inconnu [" + headerRole + "]");
        }
    }

    // C - CRÉER
    @MasquerDonneesSensibles
    public Client creerClient(String nom, String email, String role) throws StripeException {
        verifierPermission(Permission.CLIENT_CREATE);

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(nom)
                .setEmail(email)
                .build();
        Customer stripeCustomer = Customer.create(params);

        Client client = new Client(null, nom, email, stripeCustomer.getId());
        clientSPI.sauvegarder(client);
        return client;
    }

    // R - LIRE
    @MasquerDonneesSensibles
    public Client obtenirClient(UUID id, String role) {
        return clientSPI.trouverParId(id)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable pour l'id : " + id));
    }

    // U - MODIFIER
    @MasquerDonneesSensibles
    public void modifierClient(UUID id, String nouveauNom, String nouvelEmail, String role) throws StripeException {
        verifierPermission(Permission.CLIENT_UPDATE);
        
        Client clientExistant = obtenirClient(id, role);

        Customer stripeCustomer = Customer.retrieve(clientExistant.getStripeCustomerId());
        CustomerUpdateParams params = CustomerUpdateParams.builder()
                .setName(nouveauNom)
                .setEmail(nouvelEmail)
                .build();
        stripeCustomer.update(params);

        Client clientModifie = new Client(clientExistant.getId(), nouveauNom, nouvelEmail, clientExistant.getStripeCustomerId());
        clientSPI.sauvegarder(clientModifie);
    }

    // D - SUPPRIMER
    @MasquerDonneesSensibles
    public void supprimerClient(UUID id, String role) throws StripeException {
        verifierPermission(Permission.CLIENT_DELETE);
        
        Client clientExistant = obtenirClient(id, role);
        Customer stripeCustomer = Customer.retrieve(clientExistant.getStripeCustomerId());
        stripeCustomer.delete();

        clientSPI.supprimer(id);
    }
    
    public List<Client> listerTousLesClients() {
        return clientSPI.listerTousLesClients();
    }
    
}