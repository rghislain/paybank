package com.paybank.hexagonal.domaine.controleurs;

import com.paybank.hexagonal.domaine.ServiceCatalogueProduit;
import com.paybank.hexagonal.domaine.Produit;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping
    public ResponseEntity<Produit> creer(@RequestBody ProduitDto dto) throws StripeException {
        return ResponseEntity.ok(serviceCatalogueProduit.creerProduit(dto.nom(), dto.prixCentimes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produit> obtenir(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceCatalogueProduit.obtenirProduit(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> modifier(@PathVariable UUID id, @RequestBody ProduitDto dto) throws StripeException {
        serviceCatalogueProduit.modifierProduit(id, dto.nom(), dto.prixCentimes());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id) throws StripeException {
        serviceCatalogueProduit.supprimerProduit(id);
        return ResponseEntity.ok().build();
    }

    public record ProduitDto(String nom, long prixCentimes) {}
}