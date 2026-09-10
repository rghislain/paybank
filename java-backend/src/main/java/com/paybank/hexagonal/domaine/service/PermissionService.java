package com.paybank.hexagonal.domaine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Point d'entrée pour la vérification et la modification des autorisations
 * par rôle. Délègue entièrement à GestionDroitsService, qui est la seule
 * source de vérité pour la table role_permissions (role, ressource, action, granted).
 *
 * Cette classe existe pour ne pas casser le code appelant existant
 * (ex. un aspect/filtre de sécurité type @RequireDroit) qui dépend de
 * verifierAutorisation(...). Si rien n'appelle plus cette classe après
 * vérification, elle peut être supprimée au profit d'un appel direct
 * à GestionDroitsService.
 */
@Service
public class PermissionService {

    @Autowired
    private GestionDroitsService gestionDroitsService;

    /**
     * Crée ou met à jour un droit précis pour un rôle donné.
     */
    public void sauvegarderOuMettreAJour(String roleCible, String ressource, String action, boolean isGranted) {
        gestionDroitsService.mettreAJourPermissionParRole(roleCible, ressource, action, isGranted);
    }

    /**
     * Vérifie si un rôle a le droit d'effectuer une action sur une ressource.
     * Un ADMIN a toujours tous les droits par défaut, indépendamment
     * du contenu de role_permissions.
     */
    public boolean verifierAutorisation(String role, String ressource, String action) {
        if (role == null || role.isBlank()) {
            return false;
        }

        String roleNormalise = role.toUpperCase().trim();
        if ("ADMIN".equals(roleNormalise) || "ADMINISTRATEUR".equals(roleNormalise)) {
            return true;
        }

        Boolean granted = gestionDroitsService.obtenirPermission(roleNormalise, ressource, action);
        return Boolean.TRUE.equals(granted);
    }
}