package com.paybank.hexagonal.DTO;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // S'applique au niveau des méthodes des contrôleurs
@Retention(RetentionPolicy.RUNTIME) // Disponible pendant l'exécution
public @interface SecuredPermission {
    String ressource(); // Ex: "paiements", "produits"
    String action();    // Ex: "CREER", "MODIFIER", "SUPPRIMER"
}