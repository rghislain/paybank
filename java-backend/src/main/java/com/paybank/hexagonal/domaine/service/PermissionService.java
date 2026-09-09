package com.paybank.hexagonal.domaine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

/*
@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public void sauvegarderOuMettreAJour(String roleCible, String ressource, String action, boolean isGranted) {
        Optional<PermissionEntity> existant = permissionRepository.findByRoleCibleAndRessourceAndAction(roleCible, ressource, action);

        PermissionEntity permission;
        if (existant.isPresent()) {
            permission = existant.get();
            permission.setGranted(isGranted);
        } else {
            permission = new PermissionEntity();
            permission.setRoleCible(roleCible);
            permission.setRessource(ressource);
            permission.setAction(action);
            permission.setGranted(isGranted);
        }
        permissionRepository.save(permission);
    }
    
    // Méthode pour vérifier si un rôle a le droit d'effectuer une action
    public boolean verifierAutorisation(String role, String ressource, String action) {
        // Optionnel : un ADMIN a toujours tous les droits par défaut
        if ("ADMIN".equals(role) || "ADMINISTRATEUR".equals(role)) {
            return true;
        }

        // Sinon, on interroge la table role_permissions
        Optional<PermissionEntity> permission = permissionRepository
            .findByRoleCibleAndRessourceAndAction(role, ressource, action);

        // Si la ligne existe et que is_granted est true, c'est bon
        return permission.isPresent() && permission.get().isGranted();
    }
    
}
*/

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