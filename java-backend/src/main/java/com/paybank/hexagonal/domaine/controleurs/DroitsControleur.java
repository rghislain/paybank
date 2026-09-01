package com.paybank.hexagonal.domaine.controleurs;

import com.paybank.hexagonal.DTO.*;
import com.paybank.hexagonal.domaine.GestionDroitsService;
import com.paybank.hexagonal.domaine.OperateurCourantService;
import com.paybank.hexagonal.domaine.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/droits")
//@CrossOrigin(origins = "*") // Permet d'éviter les blocages CORS
public class DroitsControleur {

    @Autowired
    private GestionDroitsService gestionDroitsService;

    @Autowired
    private OperateurCourantService operateurCourantService;
    
    private final JdbcTemplate jdbcTemplate;
    
    public DroitsControleur(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    // 1. Endpoint GET pour charger la matrice des droits depuis la table 'ressources'
    /*
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenirTousLesDroits() {
        String sql = "SELECT utilisateurs_id, clients, paiements, produits, rapports_financiers, parametres_systemes, creer, lire, modifier, sauvegarder, supprimer, imprimer FROM ressources";
        List<Map<String, Object>> droits = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(droits);
    }
    */
    
    //@GetMapping("/droits")
    @GetMapping
    public ResponseEntity<?> obtenirTousLesDroits() {
        List<Map<String, Object>> resultat = new ArrayList<>();
        List<String> roles = List.of("ADMIN", "MANAGER", "EMPLOYE");

        for (String role : roles) {
            Map<String, Object> droitsRole = new HashMap<>();
            droitsRole.put("role", role);

            // 1. Récupérer les actions depuis la table 'utilisateurs'
            try {
                Map<String, Object> actions = jdbcTemplate.queryForMap(
                    "SELECT creer, lire, modifier, supprimer, sauvegarder, imprimer, envoyer FROM utilisateurs WHERE UPPER(role) = ? LIMIT 1",
                    role
                );
                droitsRole.putAll(actions);
            } catch (Exception e) {
                droitsRole.put("creer", false);
                droitsRole.put("lire", false);
                droitsRole.put("modifier", false);
                droitsRole.put("supprimer", false);
                droitsRole.put("sauvegarder", false);
                droitsRole.put("imprimer", false);
                droitsRole.put("envoyer", false);
            }

            // 2. Récupérer les ressources depuis la table 'ressources' via la liaison utilisateur
            try {
                Map<String, Object> ressources = jdbcTemplate.queryForMap(
                    "SELECT clients, paiements, produits, rapports_financiers, parametres_systemes, r.utilisateurs FROM ressources r JOIN utilisateurs u ON r.utilisateurs_id = u.id WHERE UPPER(u.role) = ? LIMIT 1",
                    role
                );
                droitsRole.putAll(ressources);
            } catch (Exception e) {
                droitsRole.put("clients", false);
                droitsRole.put("paiements", false);
                droitsRole.put("produits", false);
                droitsRole.put("rapports_financiers", false);
                droitsRole.put("parametres_systemes", false);
                droitsRole.put("utilisateurs", false);
            }

            resultat.add(droitsRole);
        }

        return ResponseEntity.ok(resultat);
    }
    
    
    /*
    // 2. Endpoint POST pour modifier un droit dynamiquement
    @PostMapping("/modifier")
    public ResponseEntity<?> modifierDroit(@RequestBody DroitRequest request) {
        String colonne = request.getRessource() != null ? request.getRessource() : request.getAction();
        
        if (colonne == null || request.getRole() == null) {
            return ResponseEntity.badRequest().body("Paramètres manquants (role ou ressource)");
        }

        List<String> colonnesAutorisees = List.of(
            "clients", "paiements", "produits", "rapports_financiers", "parametres_systemes",
            "creer", "lire", "modifier", "supprimer", "sauvegarder", "imprimer"
        );
        
        String colLower = colonne.toLowerCase();
        
        if (!colonnesAutorisees.contains(colLower)) {
            return ResponseEntity.badRequest().body("Ressource ou action non reconnue : " + colLower);
        }

        // Mise à jour de la table 'ressources' en ciblant 'utilisateurs_id'
        String sql = "UPDATE ressources SET " + colLower + " = ? WHERE UPPER(utilisateurs_id) = UPPER(?)";
        int lignesMisesAJour = jdbcTemplate.update(sql, request.isGranted(), request.getRole());

        System.out.println("🔍 Mise à jour des droits -> Colonne: " + colLower + " | ID: " + request.getRole() + " | Valeur: " + request.isGranted() + " | Lignes modifiées: " + lignesMisesAJour);

        return ResponseEntity.ok().body(Map.of("success", true, "lignesMisesAJour", lignesMisesAJour));
    }
    */
    
    /*
    @PostMapping("/modifier")
    public ResponseEntity<?> modifierDroit(@RequestBody DroitRequest request) {
        String colonne = request.getRessource() != null ? request.getRessource() : request.getAction();
        
        if (colonne == null || request.getRole() == null) {
            return ResponseEntity.badRequest().body("Paramètres manquants (role ou ressource)");
        }

        List<String> modulesRessources = List.of(
            "clients", "paiements", "produits", "rapports_financiers", "parametres_systemes"
        );
        
        List<String> actionsUtilisateurs = List.of(
            "creer", "lire", "modifier", "supprimer", "sauvegarder", "imprimer"
        );
        
        String colLower = colonne.toLowerCase();
        int lignesMisesAJour = 0;
        String roleCible = request.getRole().toUpperCase();

        if (modulesRessources.contains(colLower)) {
            // 1. C'est un module -> Table 'ressources' (avec Upsert au cas où la ligne du rôle n'existe pas)
            String sql = "INSERT INTO ressources (id, utilisateurs_id, " + colLower + ") " +
                         "VALUES (gen_random_uuid(), ?, ?) " +
                         "ON CONFLICT (utilisateurs_id) " +
                         "DO UPDATE SET " + colLower + " = EXCLUDED." + colLower;
            lignesMisesAJour = jdbcTemplate.update(sql, roleCible, request.isGranted());

        } else if (actionsUtilisateurs.contains(colLower)) {
            // 2. C'est une action -> Table 'utilisateurs' (mise à jour pour tous les utilisateurs de ce rôle)
            String sql = "UPDATE utilisateurs SET " + colLower + " = ? WHERE UPPER(role) = ?";
            lignesMisesAJour = jdbcTemplate.update(sql, request.isGranted(), roleCible);

        } else {
            return ResponseEntity.badRequest().body("Ressource ou action non reconnue : " + colLower);
        }

        System.out.println("🔍 Mise à jour des droits -> Colonne: " + colLower + " | Rôle: " + roleCible + " | Valeur: " + request.isGranted() + " | Lignes modifiées: " + lignesMisesAJour);

        return ResponseEntity.ok().body(Map.of("success", true, "lignesMisesAJour", lignesMisesAJour));
    }
    */
    
    /*
    @PostMapping("/modifier")
    public ResponseEntity<?> modifierDroit(@RequestBody DroitRequest request) {
        String colonne = request.getRessource() != null ? request.getRessource() : request.getAction();
        
        if (colonne == null || request.getRole() == null) {
            return ResponseEntity.badRequest().body("Paramètres manquants (role ou ressource)");
        }

        List<String> modulesRessources = List.of(
            "clients", "paiements", "produits", "rapports_financiers", "parametres_systemes"
        );
        
        List<String> actionsUtilisateurs = List.of(
            "creer", "lire", "modifier", "supprimer", "sauvegarder", "imprimer"
        );
        
        String colLower = colonne.toLowerCase();
        String roleCible = request.getRole().toUpperCase();
        int lignesMisesAJour = 0;

        // 1. Récupérer le VRAI ID de l'utilisateur (UUID ou String) depuis la table 'utilisateurs' via son rôle
        String utilisateurId;
        try {
            utilisateurId = jdbcTemplate.queryForObject(
                "SELECT id FROM utilisateurs WHERE UPPER(role) = ? LIMIT 1", 
                String.class, 
                roleCible
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Aucun utilisateur trouvé en BDD pour le rôle : " + roleCible);
        }

        if (modulesRessources.contains(colLower)) {
            // 2. Table 'ressources' : On insère le VRAI ID utilisateur dans 'utilisateurs_id' 
            // et on applique la valeur booléenne (true/false) du droit coché.
            String sql = "INSERT INTO ressources (id, utilisateurs_id, " + colLower + ") " +
                         "VALUES (gen_random_uuid(), ?, ?) " +
                         "ON CONFLICT (utilisateurs_id) " +
                         "DO UPDATE SET " + colLower + " = EXCLUDED." + colLower;
            
            lignesMisesAJour = jdbcTemplate.update(sql, utilisateurId, request.isGranted());

        } else if (actionsUtilisateurs.contains(colLower)) {
            // 3. Table 'utilisateurs' : Mise à jour directe de l'action pour cet utilisateur
            String sql = "UPDATE utilisateurs SET " + colLower + " = ? WHERE id = ?";
            lignesMisesAJour = jdbcTemplate.update(sql, request.isGranted(), utilisateurId);

        } else {
            return ResponseEntity.badRequest().body("Ressource ou action non reconnue : " + colLower);
        }

        System.out.println("🔍 Mise à jour réussie -> Colonne: " + colLower + " | ID Utilisateur: " + utilisateurId + " | Valeur: " + request.isGranted() + " | Lignes modifiées: " + lignesMisesAJour);

        return ResponseEntity.ok().body(Map.of("success", true, "lignesMisesAJour", lignesMisesAJour));
    }
    */
    
    @PostMapping("/modifier")
    public ResponseEntity<?> modifierDroit(@RequestBody DroitRequest request) {
        // ⚠️ Volontairement gaté sur le rôle ADMIN réel (hors matrice), et non via @RequireDroit :
        //    si on gatait la modification de la matrice PAR la matrice elle-même, un mauvais
        //    réglage pourrait rendre la matrice impossible à corriger par quiconque (verrou total).
        try {
            Role roleReel = operateurCourantService.getRoleConnecte();
            if (roleReel != Role.ADMIN) {
                return ResponseEntity.status(403).body(
                    "Accès refusé : seul un ADMIN peut modifier la matrice de droits."
                );
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }

        if (request.getRole() == null) {
            return ResponseEntity.badRequest().body("Paramètre manquant : role");
        }

        String roleCible = request.getRole().toUpperCase();
        boolean granted = request.isGranted();
        int totalModifications = 0;

        // Récupérer le vrai ID utilisateur pour la table 'ressources'
        String utilisateurId = null;
        try {
            utilisateurId = jdbcTemplate.queryForObject(
                "SELECT id FROM utilisateurs WHERE UPPER(role) = ? LIMIT 1", 
                String.class, 
                roleCible
            );
        } catch (Exception e) {
            // Géré si l'utilisateur n'est pas trouvé
        }

        // 1. Premier IF : Traitement de la table 'ressources' (Modules)
        if (request.getRessource() != null && !request.getRessource().isBlank()) {
            String colLower = request.getRessource().toLowerCase();
            List<String> modulesRessources = List.of("clients", "paiements", "produits", "rapports_financiers", "parametres_systemes", "utilisateurs");
            
            if (modulesRessources.contains(colLower) && utilisateurId != null) {
                String sql = "INSERT INTO ressources (id, utilisateurs_id, " + colLower + ") " +
                             "VALUES (gen_random_uuid(), ?, ?) " +
                             "ON CONFLICT (utilisateurs_id) " +
                             "DO UPDATE SET " + colLower + " = EXCLUDED." + colLower;
                
                int lignes = jdbcTemplate.update(sql, utilisateurId, granted);
                totalModifications += lignes;
                System.out.println("🔍 Mise à jour RESSOURCES -> Colonne: " + colLower + " | Valeur: " + granted + " | Lignes modifiées: " + lignes);
            }
        }

        // 2. Deuxième IF : Traitement de la table 'utilisateurs' (Actions)
        if (request.getAction() != null && !request.getAction().isBlank()) {
            String colLower = request.getAction().toLowerCase();
            List<String> actionsUtilisateurs = List.of("creer", "lire", "modifier", "supprimer", "sauvegarder", "imprimer", "envoyer");
            
            if (actionsUtilisateurs.contains(colLower)) {
                String sql = "UPDATE utilisateurs SET " + colLower + " = ? WHERE UPPER(role) = ?";
                int lignes = jdbcTemplate.update(sql, granted, roleCible);
                totalModifications += lignes;
                System.out.println("🔍 Mise à jour UTILISATEURS -> Colonne: " + colLower + " | Rôle: " + roleCible + " | Valeur: " + granted + " | Lignes modifiées: " + lignes);
            }
        }

        if (totalModifications == 0) {
            return ResponseEntity.badRequest().body("Aucune ressource ou action valide n'a pu être mise à jour.");
        }

        return ResponseEntity.ok().body(Map.of("success", true, "lignesMisesAJour", totalModifications));
    }
    
    // DTO interne pour mapper la requête JSON du frontend
    public static class DroitRequest {
        private String role;
        private String ressource;
        private String action;
        private boolean granted;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getRessource() { return ressource; }
        public void setRessource(String ressource) { this.ressource = ressource; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public boolean isGranted() { return granted; }
        public void setGranted(boolean granted) { this.granted = granted; }
    }
}