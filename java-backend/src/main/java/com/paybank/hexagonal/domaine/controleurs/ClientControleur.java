package com.paybank.hexagonal.domaine.controleurs;

import com.paybank.hexagonal.DTO.SecuredPermission;
import com.paybank.hexagonal.domaine.Client;
import com.paybank.hexagonal.domaine.ServiceGestionClient;
import com.stripe.exception.StripeException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
//@CrossOrigin(origins = "*") // Permet à ta page web d'appeler l'API sans blocage CORS
//@CrossOrigin(
	    //origins = "*", 
	    //allowedHeaders = {"X-Auth-Role", "Content-Type", "Authorization"},
	    //methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
	//)
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ClientControleur {

    private ServiceGestionClient gestionClientService = null;

    public ClientControleur(ServiceGestionClient gestionClientService) {
        this.gestionClientService = gestionClientService;
    }

    /*
    @PostMapping
    public ResponseEntity<Client> creer(@RequestBody ClientDto dto) throws StripeException {
        Client client = gestionClientService.creerClient(dto.nom(), dto.email());
        return ResponseEntity.ok(client);
    }
    */
    
    @SecuredPermission(ressource = "clients", action = "CREER")
    @PostMapping
    public ResponseEntity<Client> creer(
            @RequestBody ClientDto dto, 
            @RequestHeader(value = "X-Auth-Role", required = false) String role) throws StripeException {
        // On passe maintenant le rôle reçu au service de domaine !
        Client client = gestionClientService.creerClient(dto.nom(), dto.email(), role);
        return ResponseEntity.ok(client);
    }

    /*
    @GetMapping("/{id}")
    public ResponseEntity<Client> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(gestionClientService.obtenirClient(id));
    }
    */
    
    @GetMapping("/{id}")
    public ResponseEntity<Client> obtenir(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) {
        return ResponseEntity.ok(gestionClientService.obtenirClient(id, role));
    }

    /*
    @PutMapping("/{id}")
    public ResponseEntity<Void> modifier(@PathVariable UUID id, @RequestBody ClientDto dto) throws StripeException {
        gestionClientService.modifierClient(id, dto.nom(), dto.email());
        return ResponseEntity.ok().build();
    }
    */
    
    @SecuredPermission(ressource = "clients", action = "MODIFIER")
    @PutMapping("/{id}")
    public ResponseEntity<?> modifier(
            @PathVariable UUID id, 
            @RequestBody ClientDto dto,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) throws StripeException {
        gestionClientService.modifierClient(id, dto.nom(), dto.email(), role);
        return ResponseEntity.ok("Client modifié avec succès");//.build();
    }
    

    /*
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id) throws StripeException {
        gestionClientService.supprimerClient(id);
        return ResponseEntity.ok().build();
    }
    */
    
    /*
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) throws StripeException {
        gestionClientService.supprimerClient(id, role);
        return ResponseEntity.ok().build();
    }
    */
    /*
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerClient(
            @PathVariable UUID id, 
            @RequestHeader(value = "X-Auth-Role", required = false) String role) {
        try {
            // Appel de votre service
            gestionClientService.supprimerClient(id, role);
            
            // Message de succès
            return ResponseEntity.ok("Client supprimé avec succès.");
            
        } catch (SecurityException | IllegalArgumentException e) {
            // Erreur de droits (ex: Rôle EMPLOYE ou non autorisé) -> Statut 403
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Action refusée : Vous n'avez pas les droits pour supprimer ce client.");
                
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Erreur si le client a des paiements rattachés en BDD -> Statut 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Impossible de supprimer ce client : il possède des paiements ou des transactions enregistrés.");
                
        } catch (Exception e) {
            // Autres erreurs (Stripe ou techniques) -> Statut 500 mais avec un message clair
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la suppression : " + e.getMessage());
        }
    }
    */
    
    @SecuredPermission(ressource = "clients", action = "SUPPRIMER")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> supprimerClient(
            @PathVariable UUID id, 
            @RequestHeader(value = "X-Auth-Role", required = false) String role) {
        try {
            // Appel de votre service
            gestionClientService.supprimerClient(id, role);
            
            // Succès en JSON
            return ResponseEntity.ok(new MessageResponse("Client supprimé avec succès."));
            
        } catch (SecurityException | IllegalArgumentException e) {
            // Erreur de droits (Employé / Manager non autorisé) -> Statut 403 en JSON
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage())); // Transmet le message exact du domaine
                
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Erreur contrainte BDD -> Statut 400 en JSON
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Impossible de supprimer ce client : il possède des paiements ou des transactions enregistrés."));
                
        } catch (Exception e) {
            // Erreur technique -> Statut 500 en JSON
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la suppression : " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Client>> obtenirTousLesClients() {
        List<Client> clients = gestionClientService.listerTousLesClients();
        return ResponseEntity.ok(clients);
    }

    // Record DTO pour intercepter le JSON
    public record ClientDto(String nom, String email) {}
 // Record pour renvoyer des messages propres en JSON au front-end
    public record MessageResponse(String message) {}
}