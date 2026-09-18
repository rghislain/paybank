package com.paybank.hexagonal.sortie.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.paybank.hexagonal.domaine.model.Produit;

public interface ProduitSPI {
    void sauvegarder(Produit produit);
    Optional<Produit> trouverParId(UUID id);
    void supprimer(UUID id);
    List<Produit> listerTous();
}