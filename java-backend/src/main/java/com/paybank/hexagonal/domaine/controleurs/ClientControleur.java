package com.paybank.hexagonal.domaine.controleurs;

import com.paybank.hexagonal.domaine.Client;
import com.paybank.hexagonal.domaine.ServiceGestionClient;
import com.stripe.exception.StripeException;
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
    
    @PutMapping("/{id}")
    public ResponseEntity<Void> modifier(
            @PathVariable UUID id, 
            @RequestBody ClientDto dto,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) throws StripeException {
        gestionClientService.modifierClient(id, dto.nom(), dto.email(), role);
        return ResponseEntity.ok().build();
    }
    

    /*
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id) throws StripeException {
        gestionClientService.supprimerClient(id);
        return ResponseEntity.ok().build();
    }
    */
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Auth-Role", required = false) String role) throws StripeException {
        gestionClientService.supprimerClient(id, role);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    public ResponseEntity<List<Client>> obtenirTousLesClients() {
        List<Client> clients = gestionClientService.listerTousLesClients();
        return ResponseEntity.ok(clients);
    }

    // Record DTO pour intercepter le JSON
    public record ClientDto(String nom, String email) {}
}