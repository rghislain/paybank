package com.paybank.hexagonal.domaine.model;

public final class MontantCentimes {
    private final int valeur;

    public MontantCentimes(int valeur) {
        if (valeur <= 0) {
            throw new IllegalArgumentException("Le montant d'une transaction doit être strictement positif.");
        }
        this.valeur = valeur;
    }

    public int getValeur() {
        return valeur;
    }
}
