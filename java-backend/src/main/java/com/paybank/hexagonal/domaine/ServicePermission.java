package com.paybank.hexagonal.domaine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.paybank.hexagonal.repository.PermissionRepository;
import java.util.Optional;
import com.paybank.hexagonal.entity.Permission;

@Service
public class ServicePermission {

    @Autowired
    private PermissionRepository permissionRepository;

    public void sauvegarderOuMettreAJour(String roleCible, String ressource, String action, boolean isGranted) {
        Optional<Permission> existant = permissionRepository.findByRoleCibleAndRessourceAndAction(roleCible, ressource, action);

        Permission permission;
        if (existant.isPresent()) {
            permission = existant.get();
            permission.setGranted(isGranted);
        } else {
            permission = new Permission();
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
        Optional<Permission> permission = permissionRepository
            .findByRoleCibleAndRessourceAndAction(role, ressource, action);

        // Si la ligne existe et que is_granted est true, c'est bon
        return permission.isPresent() && permission.get().isGranted();
    }
    
}