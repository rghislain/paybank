package com.paybank.hexagonal.domaine.service;

import com.paybank.hexagonal.configuration.SecurityInterceptor;
import com.paybank.hexagonal.domaine.Produit;
import com.paybank.hexagonal.domaine.annotation.SecuredPermission;
import com.paybank.hexagonal.port.ProduitSPI;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductUpdateParams;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/*
 * Lors d'une modification de prix, 
 * Stripe interdit de modifier un Prix existant
 * (pour des raisons d'historique de facturation).
 * La bonne pratique Stripe consiste à créer un nouveau
 * tarif et à désactiver l'ancien, ou simplement mettre
 * à jour le produit localement.
 */
@Service
public class CatalogueProduitService {	
	private ProduitSPI produitSPI = null;

    public CatalogueProduitService(ProduitSPI produitRepository) {
        this.produitSPI = produitRepository;
    }

    //- CRÉER PRODUIT + TARIF
    public Produit creerProduit(String nom, long prixCentimes) throws StripeException {   	
    	//1. Créer le Produit sur Stripe
        ProductCreateParams productParams = ProductCreateParams.builder()
                .setName(nom)
                .build();
        Product stripeProduct = Product.create(productParams);
        //2. Créer le Tarif lié à ce produit sur Stripe
        PriceCreateParams priceParams = PriceCreateParams.builder()
                .setProduct(stripeProduct.getId())
                .setUnitAmount(prixCentimes)
                .setCurrency("eur")
                .build();
        Price stripePrice = Price.create(priceParams);
        //3. Sauvegarder dans ta BDD Supabase
        Produit produit = new Produit(null, nom, prixCentimes, stripeProduct.getId(), stripePrice.getId());
        produitSPI.sauvegarder(produit);
        return produit;
    }

    //- LIRE
    public Produit obtenirProduit(UUID id) {
        return produitSPI.trouverParId(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));
    }

    //- MODIFIER (Nom et/ou Nouveau Prix)
    public void modifierProduit(UUID id, String nouveauNom, long nouveauPrixCentimes) throws StripeException {
    	Produit produitExistant = obtenirProduit(id);
        String priceId = produitExistant.getStripePriceId();
        //1. Mettre à jour le nom sur Stripe
        Product stripeProduct = Product.retrieve(produitExistant.getStripeProductId());
        ProductUpdateParams productParams = ProductUpdateParams.builder()
                .setName(nouveauNom)
                .build();
        stripeProduct.update(productParams);
        //2. Si le prix a changé, on doit créer un NOUVEAU prix chez Stripe
        if (produitExistant.getPrixCentimes() != nouveauPrixCentimes) {
            PriceCreateParams priceParams = PriceCreateParams.builder()
                    .setProduct(produitExistant.getStripeProductId())
                    .setUnitAmount(nouveauPrixCentimes)
                    .setCurrency("eur")
                    .build();
            Price stripePrice = Price.create(priceParams);
            priceId = stripePrice.getId();
        }
        //3. Sauvegarde en BDD
        Produit produitModifie = new Produit(produitExistant.getId(), nouveauNom, nouveauPrixCentimes, produitExistant.getStripeProductId(), priceId);
        produitSPI.sauvegarder(produitModifie);
    }

    //- SUPPRIMER  
    public void supprimerProduit(UUID id) throws StripeException {  
    	Produit produit = obtenirProduit(id);
        //Désactivation du produit sur Stripe (Stripe ne supprime pas définitivement les objets financiers)
        Product stripeProduct = Product.retrieve(produit.getStripeProductId());
        ProductUpdateParams params = ProductUpdateParams.builder().setActive(false).build();
        stripeProduct.update(params);
        // Suppression locale
        produitSPI.supprimer(id);
    }
    
    public List<Produit> listerTousLesProduits() {
        return produitSPI.listerTous();
    }
}