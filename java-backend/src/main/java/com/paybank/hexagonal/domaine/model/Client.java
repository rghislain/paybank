package com.paybank.hexagonal.domaine.model;

import java.util.UUID;

public class Client {
    private final UUID id;
    private final String nom;
    private final String email;
    private final String stripeCustomerId; 

    public Client(UUID id, String nom, String email, String stripeCustomerId) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.nom = nom;
        this.email = email;
        this.stripeCustomerId = stripeCustomerId;
    }

    public UUID getId() { return id; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getStripeCustomerId() { return stripeCustomerId; }
}