package com.paybank.hexagonal.domaine.controleur;

import com.paybank.hexagonal.domaine.annotation.RequireDroit;
import com.paybank.hexagonal.domaine.service.GestionPaiementService;
import com.paybank.hexagonal.domaine.MatchResult;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/paiements")
public class PaiementControleur {

    private final GestionPaiementService gestionPaiementService;

    public PaiementControleur(GestionPaiementService gestionPaiementService) {
        this.gestionPaiementService = gestionPaiementService;
    }
    
    @PostMapping("/rapprochement")
    @RequireDroit(action = "creer", ressource = "rapports_financiers")
    public ResponseEntity<?> executerRapprochementBancaire(@RequestHeader(value = "X-Auth-Role", required = false) String role) throws StripeException {     
        try {
	    	//Le contrôleur appelle la logique globale que nous avons implémentée et testée
	        List<MatchResult> resultats = gestionPaiementService.executerRapprochementDepuisSources();
	        return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur interne : " + e.getMessage());
        }
	}
	    
    @PostMapping("/intent")
    @RequireDroit(action = "creer", ressource = "paiements")
    public ResponseEntity<Map<String, String>> creerIntent(@RequestBody CreationIntentDto dto) throws StripeException {
        Map<String, String> res = gestionPaiementService.creerIntentionPaiement(dto.clientId(), dto.montantCentimes());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/intent/{id}")
    @RequireDroit(action = "modifier", ressource = "paiements")
    public ResponseEntity<Void> modifierIntent(@PathVariable String id, @RequestBody ModificationIntentDto dto) throws StripeException {
        gestionPaiementService.modifierMontantPaiement(id, dto.nouveauMontantCentimes());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/intent/{id}")
    @RequireDroit(action = "supprimer", ressource = "paiements")
    public ResponseEntity<Void> annulerIntent(@PathVariable String id) throws StripeException {
        gestionPaiementService.annulerPaiement(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/solde")
    //Volontairement non géré par la matrice de droits : le solde Stripe est une info de
    //tableau de bord, visible par tout utilisateur authentifié quel que soit son rôle.
    public ResponseEntity<Map<String, Object>> obtenirSolde() throws StripeException {
        return ResponseEntity.ok(gestionPaiementService.obtenirSoldeCompte());
    }
    
    @PostMapping("/intent/{id}/synchroniser")
    @RequireDroit(action = "modifier", ressource = "paiements")
    public ResponseEntity<Void> synchroniserPaiement(@PathVariable String id) throws StripeException {
        gestionPaiementService.synchroniserStatutPaiement(id);
        return ResponseEntity.ok().build();
    }

    // DTOs
    public record CreationIntentDto(UUID clientId, long montantCentimes) {}
    public record ModificationIntentDto(long nouveauMontantCentimes) {}
}