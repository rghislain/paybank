package com.paybank.hexagonal.domaine.controleurs;

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

import com.paybank.hexagonal.DTO.AuthResponseDto;
import com.paybank.hexagonal.DTO.LoginRequestDTO;
import com.paybank.hexagonal.configuration.JwtService;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.UtilisateurRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import com.paybank.hexagonal.domaine.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")
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

    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;

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
    
    
    /*
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
    */
    
   /* 
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. Authentification et génération du token...
        String token = "TOKEN_" + request.getEmail(); // ou votre logique JWT actuelle
        
        // 2. Récupération de l'utilisateur en BDD pour obtenir son vrai rôle
        UtilisateurEntity utilisateur = utilisateurRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 3. Construction de la réponse JSON contenant le token ET le rôle
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", utilisateur.getRole()); // Ex: "MANAGER", "ADMIN", "EMPLOYE"

        return ResponseEntity.ok(response);
    }
    */
    
    /*
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
    	Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
    	
    	// 1. Valider les identifiants et générer le JWT
        String jwtToken =  ((com.paybank.hexagonal.domaine.AuthenticationService) authenticationService).authentifier(request);

        // 2. Créer le cookie HttpOnly
        ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
                .httpOnly(true)     // Empêche JavaScript d'y accéder (protection XSS)
                .secure(false)      // Mettre à TRUE en production (exige HTTPS)
                .path("/")          // Accessible sur tout le site
                .maxAge(24 * 60 * 60) // Durée de vie (ex: 1 jour en secondes)
                .sameSite("Lax")    // Protection contre les failles CSRF ("Lax" ou "Strict")
                .build();

        // 3. Renvoyer la réponse avec le cookie dans les en-têtes HTTP
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Connexion réussie");
    }
    */
    
    /*
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDTO request) {
    	try {
	    	// 1. Valide les identifiants via Spring Security
	        Authentication authentication = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
	        );
	        System.out.println(">>> Tentative de connexion-requête interceptée:l'email:"+request.getEmail());
	        
	        

	        // 2. Récupère l'entité utilisateur propre depuis la base de données
	        UtilisateurEntity user = utilisateurRepository.findByEmail(request.getEmail())
	            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
	        System.out.println(">>> Tentative de connexion-recupération de l'utilisateur:" + user.getNom());
	        
	        
	        //Utilisateur utilisateur=user.toDomain();
	        // 3. Générez votre token (ou remplacez par votre service JWT existant)
	        //String token = jwtService.generateToken(authentication.toString());
	        String token = jwtService.generateToken(authentication.getName());
	
	        // 4. Construit l'objet DTO plat (sans relation JPA circulaire)
	        AuthResponseDto response = new AuthResponseDto(token, user.getEmail(), user.getNom());
	
	        System.out.println(">>> Tentative de connexion pour l'email : " + request.getEmail());
	        // 5. Retourne la réponse JSON propre
	        return ResponseEntity.ok(response);
    	} catch (Exception e) {
            e.printStackTrace(); // 👈 Affichera la ligne exacte dans votre console Eclipse
            throw e;
        }
    }
    */
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UtilisateurEntity user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            String token = jwtService.generateToken(authentication.getName());

            // 👇 Pose le cookie HttpOnly attendu par JwtCookieFilter
            ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                    .httpOnly(true)
                    .secure(false)      // true en production (HTTPS)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            AuthResponseDto response = new AuthResponseDto(token, user.getEmail(), user.getNom(), user.getRole().name());

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
                .maxAge(0) // Expire immédiatement pour supprimer le cookie
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
            // 👇 Résout l'identité RÉELLE (jamais depuis le body de la requête, qui est falsifiable)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non authentifié.");
            }
            String email = authentication.getName();

            String roleToken = jwtService.generateRoleToken(email, request.getRoleCible().name());

            ResponseCookie cookieRoleActif = ResponseCookie.from("activeRoleToken", roleToken)
                    .httpOnly(true)
                    .secure(false) // 👈 Mettre à TRUE en production (HTTPS)
                    .path("/")
                    .maxAge(8 * 60 * 60) // 8 heures
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
    