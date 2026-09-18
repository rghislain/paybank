package com.paybank.hexagonal.domaine.model;

import java.util.UUID;

public class Produit {
    private final UUID id;
    private final String nom;
    private final long prixCentimes; //Ex: 2000 pour 20.00€
    private final String stripeProductId; //prod_XXXX
    private final String stripePriceId;   //price_XXXX

    public Produit(UUID id, String nom, long prixCentimes, String stripeProductId, String stripePriceId) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.nom = nom;
        this.prixCentimes = prixCentimes;
        this.stripeProductId = stripeProductId;
        this.stripePriceId = stripePriceId;
    }

    public UUID getId() { return id; }
    public String getNom() { return nom; }
    public long getPrixCentimes() { return prixCentimes; }
    public String getStripeProductId() { return stripeProductId; }
    public String getStripePriceId() { return stripePriceId; }
}