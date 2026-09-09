package com.paybank.hexagonal.domaine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Protège une méthode en vérifiant, AVANT son exécution, que le rôle réel
 * de l'opérateur authentifié (jamais un header client) possède le droit demandé dans la
 * matrice éditée via bilan.html / GET-POST /api/droits (table 'role_permissions',
 * triplet role/ressource/action -> granted).
 *
 * Exemple :
 *   @RequireDroit(action = "creer", ressource = "clients")
 *   @PostMapping
 *   public ResponseEntity<?> creerClient(...) { ... }
 *
 * - "action"    : une action ("creer", "lire", "modifier", "supprimer",
 *                 "sauvegarder", "imprimer", "envoyer") ou "" pour ne pas la vérifier.
 * - "ressource" : un module ("clients", "paiements", "produits", "utilisateurs",
 *                 "rapports_financiers", "parametres_systemes") ou "" pour ne pas le vérifier.
 *
 * Si seul l'un des deux est fourni, VerificationDroitsService considère le rôle autorisé
 * dès qu'AU MOINS UNE ligne correspondante de role_permissions est accordée (granted = true).
 * Si aucune des deux vérifications demandées n'est satisfaite, la méthode n'est PAS
 * exécutée : une SecurityException est levée et transformée en réponse 403 par
 * GlobalExceptionHandler.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireDroit {
    String action() default "";
    String ressource() default "";
}