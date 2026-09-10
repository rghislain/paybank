package com.paybank.hexagonal.domaine.controleur;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.paybank.hexagonal.DTO.AuthResponseDTO;
import com.paybank.hexagonal.DTO.LoginRequestDTO;
import com.paybank.hexagonal.configuration.JwtService;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.domaine.service.AuthenticationService;
import com.paybank.hexagonal.domaine.service.MultiUtilisateursPaiementService;
import com.paybank.hexagonal.entity.PasswordEntity;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.PasswordRepository;
import com.paybank.hexagonal.repository.UtilisateurRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthControleur {

    //On injecte uniquement le service du domaine
    @Autowired
    private MultiUtilisateursPaiementService authService;
    
    @Value("${user.admin.password}")
    private String adminPass;

    @Value("${user.manager1.password}")
    private String managerPass;

    @Value("${user.employe1.password}")
    private String employePass;
    
    @Autowired
    private UtilisateurRepository salarieRepository; 
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository; 

    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private PasswordRepository passwordRepository;
     
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UtilisateurEntity user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            String token = jwtService.generateToken(authentication.getName());

            //le cookie HttpOnly attendu par JwtCookieFilter
            ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                    .httpOnly(true)
                    .secure(false)      //true en production (HTTPS)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Lax")
                    .build();
            AuthResponseDTO response = new AuthResponseDTO(token, user.getEmail(), user.getNom(), user.getRole().name());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) //Expire immédiatement pour supprimer le cookie
                .build();

        ResponseCookie cookieRoleActif = ResponseCookie.from("activeRoleToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, cookieRoleActif.toString())
                .body("Déconnecté");
    }
      
    @PostMapping("/verifier-role")
    public ResponseEntity<?> verifierRole(@RequestBody RoleVerificationRequest request) {
        boolean isValid = false;

        if (request.getRoleCible() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rôle cible manquant");
        }       
        String roleName = request.getRoleCible().name();
        PasswordEntity storedPassword = passwordRepository.findByRole(roleName).orElse(null);

        if (storedPassword != null && request.getPassword() != null) {
            //Comparaison sécurisée avec le hash stocké en base
            isValid = passwordEncoder.matches(request.getPassword(), storedPassword.getPasswordHash());
        } else {
            //Fallback optionnel sur application.properties si aucun enregistrement en base n'existe encore
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
        }
        if (isValid) {
            //Résout l'identité RÉELLE (jamais depuis le body de la requête, qui est falsifiable)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié.");
            }
            String email = authentication.getName();
            String roleToken = jwtService.generateRoleToken(email, request.getRoleCible().name());
            ResponseCookie cookieRoleActif = ResponseCookie.from("activeRoleToken", roleToken)
                    .httpOnly(true)
                    .secure(false) //Mettre à TRUE en production (HTTPS)
                    .path("/")
                    .maxAge(8 * 60 * 60) //8 heures
                    .sameSite("Lax")
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookieRoleActif.toString())
                    .body(Map.of("message", "Rôle autorisé", "role", request.getRoleCible().name()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mot de passe incorrect pour ce rôle");
        }
    }
    
    @PostMapping("/modifier-mdp-standard")
    public ResponseEntity<?> modifierMdpStandard(
            @RequestHeader(value = "X-Auth-Role", required = false) String roleActif,
            @RequestBody MdpStandardRequest request) {

        //Vérification de sécurité
        if (!"ADMIN".equals(roleActif)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Seul un administrateur peut effectuer cette action.");
        }

        try {
            //Hachage du nouveau mot de passe standard
            String mdpCrypte = passwordEncoder.encode(request.getNouveauMdp());
            //Mise à jour en base pour tous les utilisateurs ayant ce rôle cible
            //(Ex: Mettre à jour le mot de passe par défaut des managers ou employés)
            utilisateurRepository.mettreAJourMotDePasseParRole(request.getRoleCible(), mdpCrypte);
            return ResponseEntity.ok("Mot de passe standard mis à jour avec succès pour le rôle : " + request.getRoleCible());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }
    
}
    
    
    // DTO interne (récupère bien l'email et le roleCible envoyés par le front-end)
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
    