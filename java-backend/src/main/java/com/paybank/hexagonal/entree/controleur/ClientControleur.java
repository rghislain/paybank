package com.paybank.hexagonal.entree.controleur;

import com.paybank.hexagonal.annotation.SecuredPermission;
import com.paybank.hexagonal.domaine.model.Client;
import com.paybank.hexagonal.domaine.service.ClientService;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientControleur {
    private ClientService clientService = null;

    public ClientControleur(ClientService clientService) {
        this.clientService = clientService;
    }
   
    @PostMapping
    public ResponseEntity<Client> creer(
            @RequestBody ClientDto dto) throws StripeException {
        //On passe maintenant le rôle reçu au service de domaine
        Client client = clientService.creerClient(dto.nom(), dto.email(), null); //role
        return ResponseEntity.ok(client);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Client> obtenir(
            @PathVariable UUID id) {
        return ResponseEntity.ok(clientService.obtenirClient(id, null));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> modifier(
            @PathVariable UUID id, 
            @RequestBody ClientDto dto) throws StripeException { 
        clientService.modifierClient(id, dto.nom(), dto.email(),null);
        return ResponseEntity.ok("Client modifié avec succès");
    }
     
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> supprimerClient(
            @PathVariable UUID id){
        try {
            //Appel du service
            clientService.supprimerClient(id, null);         
            //Succès en JSON
            return ResponseEntity.ok(new MessageResponse("Client supprimé avec succès."));          
        } catch (SecurityException | IllegalArgumentException e) {
            //Erreur de droits (Employé / Manager non autorisé) -> Statut 403 en JSON
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage())); //Transmet le message exact du domaine
                
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            //Erreur contrainte BDD -> Statut 400 en JSON
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Impossible de supprimer ce client : il possède des paiements ou des transactions enregistrés."));
                
        } catch (Exception e) {
            //Erreur technique -> Statut 500 en JSON
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la suppression : " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Client>> obtenirTousLesClients() {
        List<Client> clients = clientService.listerTousLesClients();
        return ResponseEntity.ok(clients);
    }

    //Record DTO pour intercepter le JSON
    public record ClientDto(String nom, String email) {}
    //Record pour renvoyer des messages propres en JSON au front-end
    public record MessageResponse(String message) {}
}