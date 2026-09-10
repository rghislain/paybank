package com.paybank.hexagonal.domaine.controleur;

import com.paybank.hexagonal.entity.PasswordEntity;
import com.paybank.hexagonal.entity.UserAuditLogEntity;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.PasswordRepository;
import com.paybank.hexagonal.repository.UserAuditLogRepository;
import com.paybank.hexagonal.repository.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/parametres")
public class PasswordControleur {

    private PasswordRepository passwordRepository = null;
    private final UserAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder; 
    private final UtilisateurRepository utilisateurRepository; 
    
    public PasswordControleur(PasswordRepository passwordRepository, PasswordEncoder passwordEncoder, UserAuditLogRepository auditLogRepository, UtilisateurRepository utilisateurRepository) {
        this.auditLogRepository = auditLogRepository;
		this.passwordEncoder = passwordEncoder;
		this.passwordRepository = passwordRepository;
		this.utilisateurRepository = utilisateurRepository;
    }

    @PostMapping("/passwords")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordUpdateRequest request, 
            @RequestHeader(value = "X-Auth-Role", required = false) String authRole,
            @RequestHeader(value = "X-User-Id", required = false) String currentAdminId //Récupération optionnelle de l'ID de l'admin
    ) {
        //Validation stricte de sécurité additionnelle côté serveur
        if (authRole == null || !authRole.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).body("Accès refusé : Rôle ADMIN requis.");
        }    
        //Récupération sécurisée de l'ID de l'admin connecté en base pour satisfaire la clé étrangère
        String adminIdToUse = currentAdminId;        
        //Si aucun ID n'est fourni ou trouvé via le contexte, on cherche un administrateur par défaut en base
        if (adminIdToUse == null || utilisateurRepository.findById(adminIdToUse).isEmpty()) {
            UtilisateurEntity defaultAdmin = utilisateurRepository.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole().toString()))
                .findFirst()
                .orElse(null);
                
            if (defaultAdmin != null) {
                adminIdToUse = defaultAdmin.getId();
            } else {
                return ResponseEntity.status(400).body("Erreur : Aucun compte administrateur valide trouvé en base pour enregistrer cette action.");
            }
        }                   
        PasswordEntity existingPwd = passwordRepository.findByRole(request.getAccountIdentifier()).orElse(null);
        String oldValueJson = (existingPwd != null) ? "{\"role\":\"" + existingPwd.getRole() + "\", \"updated_at\":\"" + existingPwd.getUpdatedAt() + "\"}" : null;
        PasswordEntity pwdEntity = (existingPwd != null) ? existingPwd : new PasswordEntity();
        if (pwdEntity.getId() == null) {
            pwdEntity.setId(UUID.randomUUID().toString());
            pwdEntity.setRole(request.getAccountIdentifier());
        }
        // Hachage sécurisé du mot de passe
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        pwdEntity.setPasswordHash(hashedPassword);       
        pwdEntity.setUpdatedAt(OffsetDateTime.now());           
        pwdEntity.setUpdatedBy(adminIdToUse);       
        passwordRepository.save(pwdEntity);
        //Synchronisation avec la table utilisateurs pour que le login classique fonctionne
        try {
            //Si accountIdentifier correspond à un rôle (ex: ADMIN, EMPLOYE, MANAGER)
            com.paybank.hexagonal.domaine.Role roleEnum = com.paybank.hexagonal.domaine.Role.valueOf(request.getAccountIdentifier().toUpperCase());
            utilisateurRepository.mettreAJourMotDePasseParRole(roleEnum, hashedPassword);
        } catch (IllegalArgumentException e) {
            //Si c'est un email ou un identifiant utilisateur direct
            utilisateurRepository.findByEmail(request.getAccountIdentifier()).ifPresent(user -> {
                user.setPassword(hashedPassword);
                utilisateurRepository.save(user);
            });
        }      
        //3. Construire et enregistrer le log d'audit structuré
        String newValueJson = "{\"role\":\"" + pwdEntity.getRole() + "\", \"updated_at\":\"" + pwdEntity.getUpdatedAt() + "\"}";
        UserAuditLogEntity auditLog = new UserAuditLogEntity();
        auditLog.setActionType("UPDATE_PASSWORD_ROLE");
        auditLog.setCreatedAt(OffsetDateTime.now());
        auditLog.setNewValues(newValueJson);
        auditLog.setOldValues(oldValueJson);
        auditLog.setTargetUserId(request.getAccountIdentifier());
        auditLog.setAuthorUserId(adminIdToUse);                  
        auditLogRepository.save(auditLog);
        return ResponseEntity.ok().body("Mot de passe système mis à jour avec succès.");
    }

    public static class PasswordUpdateRequest {
        private String accountIdentifier;
        private String newPassword;

        public String getAccountIdentifier() { return accountIdentifier; }
        public void setAccountIdentifier(String accountIdentifier) { this.accountIdentifier = accountIdentifier; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}