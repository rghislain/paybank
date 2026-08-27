package com.paybank.hexagonal.domaine.controleurs;

import com.paybank.hexagonal.DTO.SecuredPermission;
import com.paybank.hexagonal.domaine.MatchResult;
import com.paybank.hexagonal.domaine.ServiceGestionPaiement;
import com.stripe.exception.StripeException;

import org.springframework.http.HttpStatus;
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
    
    @SecuredPermission(ressource = "paiements", action = "CREER")
    @PostMapping("/rapprochement")
    public ResponseEntity<?> executerRapprochementBancaire(@RequestHeader(value = "X-Auth-Role", required = false) String role) throws StripeException {
    	// 1. Bloquer l'employé (ou si aucun rôle n'est fourni)
        if (role == null || role.trim().isEmpty() || "EMPLOYE".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Accès refusé : Un employé n'a pas les droits pour effectuer le rapprochement bancaire.");
        }
        try {
	    	// Le contrôleur appelle la logique globale que nous avons implémentée et testée
	        List<MatchResult> resultats = gestionPaiementService.executerRapprochementDepuisSources();
	        return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur interne : " + e.getMessage());
        }
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
    
    @SecuredPermission(ressource = "paiements", action = "CREER")
    @PostMapping("/intent")
    public ResponseEntity<Map<String, String>> creerIntent(@RequestBody CreationIntentDto dto) throws StripeException {
        Map<String, String> res = gestionPaiementService.creerIntentionPaiement(dto.clientId(), dto.montantCentimes());
        return ResponseEntity.ok(res);
    }

    @SecuredPermission(ressource = "paiements", action = "MODIFIER")
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