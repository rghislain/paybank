package com.paybank.hexagonal.domaine.controleurs;

import com.paybank.hexagonal.domaine.MatchResult;
import com.paybank.hexagonal.domaine.ServiceGestionPaiement;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/paiements")
@CrossOrigin(
    origins = "*", 
    allowedHeaders = {"X-Auth-Role", "Content-Type", "Authorization"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
public class PaiementControleur {

    private final ServiceGestionPaiement gestionPaiementService;

    public PaiementControleur(ServiceGestionPaiement gestionPaiementService) {
        this.gestionPaiementService = gestionPaiementService;
    }

    // ➕ NOUVEL ENDPOINT DEMANDÉ PAR L'IHM POUR LE RAPPROCHEMENT BANCAIRE
    @PostMapping("/rapprochement")
    public ResponseEntity<List<MatchResult>> executerRapprochementBancaire() throws StripeException {
        // Le contrôleur appelle la logique globale que nous avons implémentée et testée
        List<MatchResult> resultats = gestionPaiementService.executerRapprochementDepuisSources();
        return ResponseEntity.ok(resultats);
    }
    
    
    /*
    @PostMapping("/rapprochement")
    public ResponseEntity<String> executerRapprochementBancaire() {
        try {
            List<MatchResult> resultats = gestionPaiementService.executerRapprochementDepuisSources();
            
            // Conversion propre des résultats en JSON string pour l'envoyer en texte/json
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String jsonResultats = mapper.writeValueAsString(resultats);
            
            return ResponseEntity.ok(jsonResultats);
        } catch (IllegalArgumentException | SecurityException e) {
            // Renvoie un 403 Forbidden propre avec le message d'erreur du domaine
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            // Renvoie un 500 géré si une autre erreur survient
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    */

    @PostMapping("/intent")
    public ResponseEntity<Map<String, String>> creerIntent(@RequestBody CreationIntentDto dto) throws StripeException {
        Map<String, String> res = gestionPaiementService.creerIntentionPaiement(dto.clientId(), dto.montantCentimes());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/intent/{id}")
    public ResponseEntity<Void> modifierIntent(@PathVariable String id, @RequestBody ModificationIntentDto dto) throws StripeException {
        gestionPaiementService.modifierMontantPaiement(id, dto.nouveauMontantCentimes());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/intent/{id}")
    public ResponseEntity<Void> annulerIntent(@PathVariable String id) throws StripeException {
        gestionPaiementService.annulerPaiement(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/solde")
    public ResponseEntity<Map<String, Object>> obtenirSolde() throws StripeException {
        return ResponseEntity.ok(gestionPaiementService.obtenirSoldeCompte());
    }
    
    @PostMapping("/intent/{id}/synchroniser")
    public ResponseEntity<Void> synchroniserPaiement(@PathVariable String id) throws StripeException {
        gestionPaiementService.synchroniserStatutPaiement(id);
        return ResponseEntity.ok().build();
    }

    // DTOs
    public record CreationIntentDto(UUID clientId, long montantCentimes) {}
    public record ModificationIntentDto(long nouveauMontantCentimes) {}
}