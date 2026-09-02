package com.paybank.hexagonal.domaine.controleur;

import com.paybank.hexagonal.DTO.*;
import com.paybank.hexagonal.domaine.service.GestionDroitsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
//@CrossOrigin(origins = "*")
public class PermissionControleur {

    @Autowired
    private GestionDroitsService gestionDroitsService;

    @PostMapping("/modifier")
    public ResponseEntity<String> modifierPermission(@RequestBody PermissionRequestDTO request) {
        try {
            gestionDroitsService.mettreAJourPermissionParRole(
                request.getRoleCible(), 
                request.getRessource(), 
                request.getAction(), 
                request.isGranted()
            );
            return ResponseEntity.ok("Permission mise à jour avec succès en BDD.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur serveur : " + e.getMessage());
        }
    }
}