package com.paybank.hexagonal.domaine.controleurs;

import com.paybank.hexagonal.domaine.ServiceCatalogueProduit;
import com.paybank.hexagonal.DTO.SecuredPermission;
import com.paybank.hexagonal.domaine.Produit;
import com.stripe.exception.StripeException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produits")
//@CrossOrigin(origins = "*")
@CrossOrigin(
	    origins = "*", 
	    allowedHeaders = {"X-Auth-Role", "Content-Type", "Authorization"},
	    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
	)
public class ProduitControleur {

    private ServiceCatalogueProduit serviceCatalogueProduit = null;

    public ProduitControleur(ServiceCatalogueProduit serviceCatalogueProduit) {
        this.serviceCatalogueProduit = serviceCatalogueProduit;
    }

    /*
    @PostMapping
    public ResponseEntity<Produit> creer(@RequestBody ProduitDto dto) throws StripeException {
        return ResponseEntity.ok(serviceCatalogueProduit.creerProduit(dto.nom(), dto.prixCentimes()));
    }
    */
    
    /*
    @PostMapping
    public ResponseEntity<String> creer(@RequestBody ProduitDto dto) {
        try {
            Produit produit = serviceCatalogueProduit.creerProduit(dto.nom(), dto.prixCentimes());
            // On peut renover le JSON du produit créé ou un statut OK simple
            return ResponseEntity.ok(produit.getId().toString()); // ou sérialiser en JSON si besoin
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    */

    @GetMapping("/{id}")
    public ResponseEntity<Produit> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceCatalogueProduit.obtenirProduit(id));
    }

    /*
    @PutMapping("/{id}")
    public ResponseEntity<Void> modifier(@PathVariable UUID id, @RequestBody ProduitDto dto) throws StripeException {
        serviceCatalogueProduit.modifierProduit(id, dto.nom(), dto.prixCentimes());
        return ResponseEntity.ok().build();
    }
    */
    
    /*
    @PutMapping("/{id}")
    public ResponseEntity<String> modifier(@PathVariable UUID id, @RequestBody ProduitDto dto) {
        try {
            serviceCatalogueProduit.modifierProduit(id, dto.nom(), dto.prixCentimes());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    */
    
    @SecuredPermission(ressource = "produits", action = "CREER")
    @PostMapping
    public ResponseEntity<String> creer(@RequestBody ProduitDto dto) {
        try {
            Produit produit = serviceCatalogueProduit.creerProduit(dto.nom(), dto.prixCentimes());
            return ResponseEntity.ok("Produit créé avec succès (ID: " + produit.getId() + ")");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @SecuredPermission(ressource = "produits", action = "MODIFIER")
    @PutMapping("/{id}")
    public ResponseEntity<String> modifier(@PathVariable UUID id, @RequestBody ProduitDto dto) {
        try {
            serviceCatalogueProduit.modifierProduit(id, dto.nom(), dto.prixCentimes());
            return ResponseEntity.ok("Produit modifié avec succès.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /*
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id) throws StripeException {
        serviceCatalogueProduit.supprimerProduit(id);
        return ResponseEntity.ok().build();
    }
    */
    
    @SecuredPermission(ressource = "produits", action = "SUPPRIMER")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(@PathVariable UUID id,
    		@RequestHeader(value = "X-Auth-Role", required = false) String role) {
        /*
    	try {
            serviceCatalogueProduit.supprimerProduit(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // Renvoie le message d'erreur précis (ex: "Interdit par le domaine...") avec un code 403 ou 400
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        */
    	try {
            // 1. Vérification des rôles (Exemple : interdire si c'est un employé ou si aucun statut)
            if (role == null || role.isEmpty() || "EMPLOYE".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Erreur : Vous n'avez pas les droits nécessaires pour supprimer un produit.");
            }

            // 2. Appel du service de suppression
            serviceCatalogueProduit.supprimerProduit(id);
            return ResponseEntity.ok("Produit supprimé avec succès.");
            
        } catch (IllegalArgumentException e) {
            // Erreur métier (ex: produit introuvable)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Erreur critique : Le produit est lié à des paiements/transactions existants
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Impossible de supprimer ce produit : il est déjà associé à des paiements existants.");
                
        } catch (Exception e) {
            // Erreur technique générale
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur interne : " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Produit>> listerTous() {
        List<Produit> produits = serviceCatalogueProduit.listerTousLesProduits();
        return ResponseEntity.ok(produits);
    }

    public record ProduitDto(String nom, long prixCentimes) {}
}