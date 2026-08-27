package com.paybank.hexagonal.domaine.controleurs;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.UtilisateurRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // On injecte uniquement le service du domaine
    @Autowired
    private ServiceMultiUtilisateursPaiement authService;
    
    @Value("${user.admin.password}")
    private String adminPass;

    @Value("${user.manager1.password}")
    private String managerPass;

    @Value("${user.employe1.password}")
    private String employePass;
    
    @Autowired
    private UtilisateurRepository salarieRepository; // ou votre service d'authentification
    
    @Autowired
    private PasswordEncoder passwordEncoder; // ou autre vérificateur de mot de passe
    
    @Autowired
    private UtilisateurRepository utilisateurRepository; // Ou votre service de gestion des utilisateurs


    /*
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            // 1. Appel de la logique métier (Domaine)
            Utilisateur user = authService.authentifier(email, password);
            
            // 2. Si authentification réussie, on génère une réponse JSON
            // Note : L'email dans le token est une solution temporaire pour votre POC
            return ResponseEntity.ok(Collections.singletonMap("token", "TOKEN_" + user.getEmail()));
            
        } catch (SecurityException e) {
            // 3. Gestion propre des erreurs de sécurité (401 Unauthorized)
            return ResponseEntity.status(401).body(Collections.singletonMap("message", e.getMessage()));
        }
    }
    */
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            // 1. Authentification métier
            Utilisateur user = authService.authentifier(email, password);
            
            // 2. CRUCIAL : Remplir le contexte de sécurité manuellement
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            return ResponseEntity.ok(Collections.singletonMap("token", "TOKEN_" + email));
                
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", e.getMessage()));
        }
    }
    
    /*
    @PostMapping("/verifier-role")
    public ResponseEntity<?> verifierRole(@RequestBody RoleVerificationRequest request) {
        boolean isValid = false;

        switch (request.getRole()) {
            case ADMIN:
                isValid = request.getPassword().equals(adminPass);
                break;
            case MANAGER:
                isValid = request.getPassword().equals(managerPass);
                break;
            case EMPLOYE:
                isValid = request.getPassword().equals(employePass);
                break;
        }

        if (isValid) {
            return ResponseEntity.ok().body("Accès autorisé");
        } else {
            return ResponseEntity.status(401).body("Mot de passe invalide");
        }
    }
    */
    
    /*
    @PostMapping("/verifier-role")
    public ResponseEntity<?> verifierRole(@RequestBody RoleVerificationRequest request) {
        // 1. Récupérer l'utilisateur connecté ou vérifier si un compte possède ce rôle / ce mot de passe
        Optional<UtilisateurEntity> salarieOpt = salarieRepository.findByEmail(request.getEmail());
        
        if (salarieOpt.isPresent()) {
            UtilisateurEntity salarie = salarieOpt.get();
            
            // Vérification du mot de passe de l'utilisateur ou d'un administrateur selon votre règle métier
            boolean passwordValid = passwordEncoder.matches(request.getPassword(), salarie.getPassword());
            
            // Si vous souhaitez qu'un utilisateur standard puisse changer de rôle s'il fournit 
            // le mot de passe d'un admin/manager existant, vous pouvez adapter la logique ici.
            if (passwordValid) {
                return ResponseEntity.ok().body("Rôle autorisé");
            }
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mot de passe incorrect");
    }
    */
    
    /*
    @PostMapping("/verifier-role")
    public ResponseEntity<?> verifierRole(@RequestBody RoleVerificationRequest request) {
        boolean isValid = false;

        if (request.getRoleCible() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rôle cible manquant");
        }

        // Vérification par rapport aux mots de passe définis dans application.properties
        switch (request.getRoleCible()) {
            case ADMIN:
                isValid = request.getPassword() != null && request.getPassword().equals(adminPass);
                break;
            case MANAGER:
                isValid = request.getPassword() != null && request.getPassword().equals(managerPass);
                break;
            case EMPLOYE:
                isValid = request.getPassword() != null && request.getPassword().equals(employePass);
                break;
            default:
                isValid = false;
        }

        if (isValid) {
            return ResponseEntity.ok().body("Rôle autorisé");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mot de passe incorrect pour ce rôle");
        }
    }
    */
    
    /*
 // DTO interne pour la requête
    public static class RoleVerificationRequest {
        private Role role;
        private String password;
        // Getters et Setters...
        public Role getRole() { return role; }
        public String getEmail() {
			// TODO Auto-generated method stub
			return null;
		}
		public void setRole(Role role) { this.role = role; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    */
    
    /*
 // DTO interne corrigé pour correspondre exactement au Front-end
    public static class RoleVerificationRequest {
        private String email;
        private Role roleCible;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public Role getRoleCible() { return roleCible; }
        public void setRoleCible(Role roleCible) { this.roleCible = roleCible; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    */
    
    @PostMapping("/verifier-role")
    public ResponseEntity<?> verifierRole(@RequestBody RoleVerificationRequest request) {
        boolean isValid = false;

        if (request.getRoleCible() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rôle cible manquant");
        }

        // Vérification par rapport aux variables injectées depuis application.properties
        switch (request.getRoleCible()) {
            case ADMIN:
                isValid = request.getPassword() != null && request.getPassword().equals(adminPass);
                break;
            case MANAGER:
                isValid = request.getPassword() != null && request.getPassword().equals(managerPass);
                break;
            case EMPLOYE:
                isValid = request.getPassword() != null && request.getPassword().equals(employePass);
                break;
            default:
                isValid = false;
        }

        if (isValid) {
            return ResponseEntity.ok().body("Rôle autorisé");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mot de passe incorrect pour ce rôle");
        }
    }
    
    @PostMapping("/modifier-mdp-standard")
    public ResponseEntity<?> modifierMdpStandard(
            @RequestHeader(value = "X-Auth-Role", required = false) String roleActif,
            @RequestBody MdpStandardRequest request) {

        // Vérification de sécurité
        if (!"ADMIN".equals(roleActif)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Seul un administrateur peut effectuer cette action.");
        }

        try {
            // Hachage du nouveau mot de passe standard
            String mdpCrypte = passwordEncoder.encode(request.getNouveauMdp());

            // Mise à jour en base pour tous les utilisateurs ayant ce rôle cible
            // (Ex: Mettre à jour le mot de passe par défaut des managers ou employés)
            utilisateurRepository.mettreAJourMotDePasseParRole(request.getRoleCible(), mdpCrypte);

            return ResponseEntity.ok("Mot de passe standard mis à jour avec succès pour le rôle : " + request.getRoleCible());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }
}
    
    
    // DTO interne corrigé (récupère bien l'email et le roleCible envoyés par le front-end)
    class RoleVerificationRequest {
        private String email;
        private Role roleCible;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public Role getRoleCible() { return roleCible; }
        public void setRoleCible(Role roleCible) { this.roleCible = roleCible; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    // DTOs associés
    class MdpStandardRequest {
        private Role roleCible;
        private String nouveauMdp;

        public Role getRoleCible() { return roleCible; }
        public void setRoleCible(Role roleCible) { this.roleCible = roleCible; }
        public String getNouveauMdp() { return nouveauMdp; }
        public void setNouveauMdp(String nouveauMdp) { this.nouveauMdp = nouveauMdp; }
    }
    
