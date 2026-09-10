package com.paybank.hexagonal.domaine.controleur;

import com.paybank.hexagonal.domaine.Client;
import com.paybank.hexagonal.domaine.annotation.SecuredPermission;
import com.paybank.hexagonal.domaine.service.GestionClientService;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientControleur {
    private GestionClientService gestionClientService = null;

    public ClientControleur(GestionClientService gestionClientService) {
        this.gestionClientService = gestionClientService;
    }
   
    @PostMapping
    public ResponseEntity<Client> creer(
            @RequestBody ClientDto dto, 
            String role) throws StripeException {
        //On passe maintenant le rôle reçu au service de domaine
        Client client = gestionClientService.creerClient(dto.nom(), dto.email(), role);
        return ResponseEntity.ok(client);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Client> obtenir(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) {
        return ResponseEntity.ok(gestionClientService.obtenirClient(id, role));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> modifier(
            @PathVariable UUID id, 
            @RequestBody ClientDto dto,
            String role) throws StripeException {
        gestionClientService.modifierClient(id, dto.nom(), dto.email(), role);
        return ResponseEntity.ok("Client modifié avec succès");
    }
     
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> supprimerClient(
            @PathVariable UUID id, 
            String role) {
        try {
            //Appel du service
            gestionClientService.supprimerClient(id, role);         
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
        List<Client> clients = gestionClientService.listerTousLesClients();
        return ResponseEntity.ok(clients);
    }

    //Record DTO pour intercepter le JSON
    public record ClientDto(String nom, String email) {}
    //Record pour renvoyer des messages propres en JSON au front-end
    public record MessageResponse(String message) {}
}