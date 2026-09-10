package com.paybank.hexagonal.domaine.controleur;

import com.paybank.hexagonal.DTO.*;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.service.GestionDroitsService;
import com.paybank.hexagonal.domaine.service.OperateurCourantService;
import com.paybank.hexagonal.entity.UserAuditLogEntity;
import com.paybank.hexagonal.repository.UserAuditLogRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import com.paybank.hexagonal.DTO.*;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.service.GestionDroitsService;
import com.paybank.hexagonal.domaine.service.OperateurCourantService;
import com.paybank.hexagonal.entity.UserAuditLogEntity;
import com.paybank.hexagonal.repository.UserAuditLogRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/droits")
public class DroitsControleur {

    @Autowired
    private GestionDroitsService gestionDroitsService;

    @Autowired
    private OperateurCourantService operateurCourantService;

    private final UserAuditLogRepository auditLogRepository;

    public DroitsControleur(UserAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Renvoie la matrice complète des droits, un enregistrement par triplet
     * { role, ressource, action, granted }. C'est exactement le format
     * attendu par chargerMatriceDroits() côté front (bilan.html), qui cible
     * ensuite chaque case précisément via ses data-attributes.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenirTousLesDroits() {
        return ResponseEntity.ok(gestionDroitsService.obtenirToutesLesPermissions());
    }

    @PostMapping("/modifier")
    public ResponseEntity<?> modifierDroit(
            @RequestBody DroitRequestDTO request,
            @RequestHeader(value = "X-Auth-Role", required = false) String authRole,
            @RequestHeader(value = "X-User-Id", required = false) String currentAdminId) {

        //--- 1. Sécurité : vérifiée EN PREMIER, avant toute écriture en base. ---
        //Volontairement géré par le rôle réellement connecté (et non via @RequireDroit) :
        //si on gérait la modification de la matrice PAR la matrice elle-même, un mauvais
        //réglage pourrait la rendre impossible à corriger par quiconque (verrou total).
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

        if (authRole == null || !authRole.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).body("Accès refusé : Rôle ADMIN requis.");
        }

        //--- 2. Validation des paramètres ---
        if (request.getRole() == null || request.getRole().name().isBlank()
                || request.getRessource() == null || request.getRessource().isBlank()
                || request.getAction() == null || request.getAction().isBlank()) {
            return ResponseEntity.badRequest().body(
                "Paramètres manquants : role, ressource et action sont tous obligatoires."
            );
        }

        String roleCible = request.getRole().toUpperCase().trim();
        String ressourceCible = request.getRessource().toLowerCase().trim();
        String actionCible = request.getAction().toLowerCase().trim();
        boolean granted = request.isGranted();

        //--- 3. Ancien état (pour l'audit log), AVANT modification ---
        Boolean ancienneValeur = gestionDroitsService.obtenirPermission(roleCible, ressourceCible, actionCible);

        //--- 4. Mise à jour du triplet dans role_permissions ---
        try {
            gestionDroitsService.mettreAJourPermissionParRole(roleCible, ressourceCible, actionCible, granted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        //--- 5. Journalisation dans user_audit_logs ---
        String oldValueJson = "{\"role\":\"" + roleCible + "\", \"ressource\":\"" + ressourceCible
                + "\", \"action\":\"" + actionCible + "\", \"granted\":"
                + (ancienneValeur == null ? "null" : ancienneValeur) + "}";

        String newValueJson = "{\"role\":\"" + roleCible + "\", \"ressource\":\"" + ressourceCible
                + "\", \"action\":\"" + actionCible + "\", \"granted\":" + granted + "}";

        UserAuditLogEntity auditLog = new UserAuditLogEntity();
        auditLog.setActionType("UPDATE_ROLE_PERMISSION");
        auditLog.setOldValues(oldValueJson);
        auditLog.setNewValues(newValueJson);
        auditLog.setCreatedAt(OffsetDateTime.now());
        auditLog.setAuthorUserId(currentAdminId != null ? currentAdminId : "ADMIN_SYSTEM");
        auditLog.setTargetUserId(roleCible);
        auditLogRepository.save(auditLog);

        return ResponseEntity.ok().body(Map.of("success", true));
    }
}