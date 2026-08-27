package com.paybank.hexagonal.domaine.controleurs;

import com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.DTO.SecuredPermission;
import com.paybank.hexagonal.domaine.Role;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(
    origins = "*", 
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
public class UtilisateurControleur {

    private final ServiceMultiUtilisateursPaiement serviceMultiUtilisateursPaiement;
    
    // 🟢 Injection automatique depuis application.properties
    @Value("${app.passwords.admin}")
    private String adminPass;

    @Value("${app.passwords.manager}")
    private String managerPass;

    @Value("${app.passwords.employe}")
    private String employePass;
    
    // Constructeur : Injection unique du service du domaine (Hexagone)
    public UtilisateurControleur(ServiceMultiUtilisateursPaiement serviceMultiUtilisateursPaiement) {
        this.serviceMultiUtilisateursPaiement = serviceMultiUtilisateursPaiement;
    }

    /*
    @PostMapping
    public ResponseEntity<Utilisateur> create(@RequestBody CreateUserRequest req, @AuthenticationPrincipal Utilisateur operator) {
        Utilisateur newUser = serviceMultiUtilisateursPaiement.createUser(operator, req.getEmail(), req.getName(), req.getRole());
        return ResponseEntity.ok(newUser);
    }
    */
    
    /*
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreateUserRequest req, 
            @RequestHeader(value = "X-Auth-Role", required = false) String roleHeader) {
        
        try {
            // Simulation dynamique de l'opérateur connecté via l'interface
            String roleString = (roleHeader != null) ? roleHeader.toUpperCase() : "EMPLOYE";
            Role roleEnumOperateur = Role.valueOf(roleString);
            Utilisateur operator = new Utilisateur("ghislainr", "admin@paybank.com", "password", Role.EMPLOYE);

            // Appel du domaine
            Utilisateur newUser = serviceMultiUtilisateursPaiement.createUser(operator); //, req.getEmail(), req.getName(), req.getRole()
            return ResponseEntity.ok(newUser);
            
        } catch (SecurityException e) {
            // Si par exemple un EMPLOYE essaie de créer un autre salarié mais n'a pas les droits
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    */
    
    /*
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreateUserRequest req, 
            @RequestHeader(value = "X-Auth-Role", required = false) String roleHeader) {
        
    	BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        try {
            String roleString = (roleHeader != null) ? roleHeader.toUpperCase() : "EMPLOYE";
            Role roleEnumOperateur = Role.valueOf(roleString);
            
            // CORRECTION : Utilisez roleEnumOperateur ici au lieu de Role.EMPLOYE
            Utilisateur operator = new Utilisateur("ghislainr", "ghislainrochette@paybank.com", "employePass1", roleEnumOperateur, true);

         // 1. Récupérer ou générer le mot de passe
            String motDePasseClair = (req.getPassword() != null && !req.getPassword().isEmpty()) 
                ? req.getPassword() 
                : "employePass1"; 

            // 2. Le HACHER avec BCrypt
            String motDePasseHashe = passwordEncoder.encode(motDePasseClair);
            // Le nouvel utilisateur à créer (récupéré depuis le JSON de la requête)
            Utilisateur nouvelUtilisateur = new Utilisateur(req.getName(), req.getEmail(), motDePasseHashe, req.getRole(), true);
            
            Utilisateur newUser = serviceMultiUtilisateursPaiement.createUser(operator, nouvelUtilisateur);
            return ResponseEntity.ok(newUser);
                
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    */
    
    @SecuredPermission(ressource = "utilisateurs", action = "CREER")
    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreateUserRequest req,  
            @RequestHeader(value = "X-Auth-Role", required = false) String roleHeader) {
        
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        try {
            String roleString = (roleHeader != null) ? roleHeader.toUpperCase() : "EMPLOYE";
            Role roleEnumOperateur = Role.valueOf(roleString);
            
            // Opérateur effectuant l'action
            Utilisateur operator = new Utilisateur("ghislainr", "ghislainrochette@paybank.com", "employePass1", roleEnumOperateur, true);

            // 1. Récupérer ou générer le mot de passe
            String motDePasseClair = (req.getPassword() != null && !req.getPassword().isEmpty()) 
                ? req.getPassword() 
                : "employePass1";  

            // 2. Le HACHER avec BCrypt
            String motDePasseHashe = passwordEncoder.encode(motDePasseClair);
            
            // 3. SOLUTION PROPRE : Utilisation du constructeur de base et des setters pour éviter les erreurs d'ordre
            Utilisateur nouvelUtilisateur = new Utilisateur();
            nouvelUtilisateur.setName(req.getNom()); // <-- On force explicitement le nom
            nouvelUtilisateur.setEmail(req.getEmail());
            nouvelUtilisateur.setPassword(motDePasseHashe);
            nouvelUtilisateur.setRole(req.getRole());
            nouvelUtilisateur.setActif(true);
            
            Utilisateur newUser = serviceMultiUtilisateursPaiement.createUser(operator, nouvelUtilisateur);
            return ResponseEntity.ok(newUser);
                
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    /*
    @PutMapping("/{id}/modifier-droits")
    public ResponseEntity<?> modifierDroits(
            @PathVariable UUID id, 
            @RequestParam String nouveauNom,
            @RequestParam String nouveauRole,
            //@RequestHeader(value = "X-Auth-Role", required = false) String roleHeader) {
            @RequestBody(required = false) Map<String, String> body, // Récupération du JSON optionnel
            @RequestHeader("X-Auth-Role") String roleOperateur){ 
    	try {
            String roleString = (roleOperateur != null) ? roleOperateur.toUpperCase() : "EMPLOYE";
            Role roleEnumOperateur = Role.valueOf(roleString);
            Utilisateur operator = new Utilisateur("operateur", "operateur@paybank.com", "pass", roleEnumOperateur, true);

            // Récupération du mot de passe s'il a été transmis dans le body JSON
            String nouveauPassword = (body != null) ? body.get("password") : null;
            Role targetRole = Role.valueOf(nouveauRole.toUpperCase());

            // Appel de la méthode du service avec le mot de passe
            Utilisateur userMisAJour = serviceMultiUtilisateursPaiement.updateUser(operator, id.toString(), nouveauNom, targetRole, nouveauPassword);

            return ResponseEntity.ok(userMisAJour);
            
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    */
    
    
    @PutMapping("/{id}/modifier-droits")
    public ResponseEntity<?> modifierDroits(
            @PathVariable UUID id, 
            @RequestBody UpdateUserRequest req, 
            //@RequestHeader("X-Auth-Role") String roleOperateur) {
            @RequestHeader(value = "X-Auth-Role", required = false) String roleOperateur) {
        
    	try {
            // Détermination automatique du mot de passe en dur selon le rôle choisi
            String passwordToUse;
            if (req.getNouveauRole() == Role.ADMIN) {
                passwordToUse = adminPass; // 👈 Récupéré de application.properties
            } else if (req.getNouveauRole() == Role.MANAGER) {
                passwordToUse = managerPass; // 👈 Récupéré de application.properties
            } else {
                passwordToUse = employePass; // 👈 Récupéré de application.properties
            }

            String roleString = (roleOperateur != null) ? roleOperateur.toUpperCase() : "EMPLOYE";
            Role roleEnumOperateur = Role.valueOf(roleString);
            Utilisateur operator = new Utilisateur("operateur", "operateur@paybank.com", "pass", roleEnumOperateur, true);
            
            // Appel de votre service pour persister en BDD (avec hachage BCrypt si besoin)
            Utilisateur userMisAJour = serviceMultiUtilisateursPaiement.updateUser(
                operator,
            	id.toString(), 
                req.getNouveauNom(), 
                req.getNouveauRole(), 
                passwordToUse
            );

            return ResponseEntity.ok(userMisAJour);
            
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Utilisateur>> obtenirTousLesSalaries() {
        List<Utilisateur> salaries = serviceMultiUtilisateursPaiement.listerTousLesSalaries();
        return ResponseEntity.ok(salaries);
    }
    
    // Endpoint pour basculer un droit globalement pour un rôle donné depuis l'interface
    @PostMapping("/droits/mettre-a-jour")
    public ResponseEntity<?> mettreAJourDroitParRole(
            @RequestParam Role role,
            @RequestParam String nomDroit,
            @RequestParam boolean valeur) {
        try {
            // Appel de la méthode de l'adaptateur via votre service
            serviceMultiUtilisateursPaiement.basculerDroitPourRole(role, nomDroit, valeur);
            return ResponseEntity.ok("Droits mis à jour avec succès pour le rôle : " + role);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Erreur lors de la mise à jour des droits : " + e.getMessage());
        }
    }

    // Classe interne DTO nécessaire pour réceptionner le JSON de la requête HTTP
    /*
    public static class CreateUserRequest {
    	private String name;
    	private String email;
    	private String password;
        private Role role;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public String getPassword() { return password; } // <-- Ajoutez le getter
        public void setPassword(String password) { this.password = password; } // <-- Ajoutez le setter
    }
    */
    
    public static class CreateUserRequest {
        private String nom; // 👈 Changé de 'name' à 'nom' pour correspondre au front-end
        private String email;
        private String password;
        private Role role;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getNom() { return nom; } // 👈 Getters et setters mis à jour
        public void setNom(String nom) { this.nom = nom; }
        
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        
        public String getPassword() { return password; } 
        public void setPassword(String password) { this.password = password; } 
    }
    
    public static class UpdateUserRequest {
        private String nouveauNom;
        private Role nouveauRole;
        private String password; // Optionnel : si vide, un mot de passe standard sera appliqué

        public String getNouveauNom() { return nouveauNom; }
        public void setNouveauNom(String nouveauNom) { this.nouveauNom = nouveauNom; }
        
        public Role getNouveauRole() { return nouveauRole; }
        public void setNouveauRole(Role nouveauRole) { this.nouveauRole = nouveauRole; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
}