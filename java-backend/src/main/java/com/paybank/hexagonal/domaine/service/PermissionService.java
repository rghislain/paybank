package com.paybank.hexagonal.domaine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.paybank.hexagonal.repository.PermissionRepository;
import java.util.Optional;
import com.paybank.hexagonal.entity.PermissionEntity;

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