package com.paybank.hexagonal.domaine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifierDroit {
    /**
     * L'action concernée par le contrôle (ex: "CREER", "LIRE", "MODIFIER", "SUPPRIMER").
     */
    String action();
}