package com.paybank.hexagonal.entree.controleur;

import com.paybank.hexagonal.annotation.SecuredPermission;
import com.paybank.hexagonal.domaine.model.Produit;
import com.paybank.hexagonal.domaine.service.CatalogueProduitService;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin(
	    origins = "*", 
	    allowedHeaders = {"Content-Type", "Authorization"}, 
	    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
	)
public class ProduitControleur {

    private CatalogueProduitService serviceCatalogueProduit = null;

    public ProduitControleur(CatalogueProduitService serviceCatalogueProduit) {
        this.serviceCatalogueProduit = serviceCatalogueProduit;
    }
  
    @GetMapping("/{id}")
    public ResponseEntity<Produit> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceCatalogueProduit.obtenirProduit(id));
    }
 
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
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(@PathVariable UUID id) {     
    	try {           
            serviceCatalogueProduit.supprimerProduit(id);
            return ResponseEntity.ok("Produit supprimé avec succès.");
            
        } catch (IllegalArgumentException e) {
            //Erreur métier (ex: produit introuvable)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            //Erreur critique : Le produit est lié à des paiements/transactions existants
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Impossible de supprimer ce produit : il est déjà associé à des paiements existants.");
                
        } catch (Exception e) {
            //Erreur technique générale
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