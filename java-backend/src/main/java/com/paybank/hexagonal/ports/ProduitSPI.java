package com.paybank.hexagonal.ports;

import com.paybank.hexagonal.domaine.Produit;
import java.util.Optional;
import java.util.UUID;

public interface ProduitSPI {
    void sauvegarder(Produit produit);
    Optional<Produit> trouverParId(UUID id);
    void supprimer(UUID id);
}