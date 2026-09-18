package com.paybank.hexagonal.entree.controleur;

import com.paybank.hexagonal.annotation.RequireDroit;
import com.paybank.hexagonal.domaine.model.Role;
import com.paybank.hexagonal.domaine.model.Utilisateur;
import com.paybank.hexagonal.domaine.service.MultiUtilisateursPaiementService;
import com.paybank.hexagonal.domaine.service.OperateurCourantService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurControleur {

    private final MultiUtilisateursPaiementService serviceMultiUtilisateursPaiement;

    @Autowired
    private OperateurCourantService operateurCourantService;
    
    //Injection automatique depuis application.properties
    @Value("${app.passwords.admin}")
    private String adminPass;

    @Value("${app.passwords.manager}")
    private String managerPass;

    @Value("${app.passwords.employe}")
    private String employePass;
    
    //Constructeur : Injection unique du service du domaine (Hexagone)
    public UtilisateurControleur(MultiUtilisateursPaiementService serviceMultiUtilisateursPaiement) {
        this.serviceMultiUtilisateursPaiement = serviceMultiUtilisateursPaiement;
    }
    
    @RequireDroit(action = "creer", ressource = "utilisateurs")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateUserRequest req) {        
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        try {
            //Le droit "creer" a déjà été vérifié par DroitAspect avant l'entrée dans cette méthode
            Utilisateur operator = operateurCourantService.getOperateurConnecte();
            //1. Récupérer ou générer le mot de passe
            String motDePasseClair = (req.getPassword() != null && !req.getPassword().isEmpty()) 
                ? req.getPassword() 
                : "employePass1";  
            //2. Le HACHER avec BCrypt
            String motDePasseHashe = passwordEncoder.encode(motDePasseClair);           
            //3. Construction du nouvel utilisateur
            Utilisateur nouvelUtilisateur = new Utilisateur();
            nouvelUtilisateur.setNom(req.getNom());
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
     
    @RequireDroit(action = "modifier", ressource = "utilisateurs")
    @PutMapping("/{id}/modifier-droits")
    public ResponseEntity<?> modifierDroits(
            @PathVariable UUID id, 
            @RequestBody UpdateUserRequest req) {       
    	try {
            //Le droit "modifier" a déjà été vérifié par DroitAspect avant l'entrée dans cette méthode
            Utilisateur operator = operateurCourantService.getOperateurConnecte();
            //Détermination automatique du mot de passe en dur selon le rôle choisi
            String passwordToUse;
            if (req.getNouveauRole() == Role.ADMIN) {
                passwordToUse = adminPass; //Récupéré de application.properties
            } else if (req.getNouveauRole() == Role.MANAGER) {
                passwordToUse = managerPass; //Récupéré de application.properties
            } else {
                passwordToUse = employePass; //Récupéré de application.properties
            }
            //Appel du service pour persister en BDD (avec hachage BCrypt si besoin)
            //ServiceMultiUtilisateursPaiement.updateUser() vérifie déjà en plus
            //qu'un MANAGER ne peut pas promouvoir quelqu'un ADMIN (défense en profondeur)
            Utilisateur userMisAJour = serviceMultiUtilisateursPaiement.updateUser(
                operator,
            	id.toString(), 
                req.getNouveauNom(), 
                req.getNouveauRole(), 
                passwordToUse
            );
            return ResponseEntity.ok(userMisAJour);            
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    
    @RequireDroit(action = "lire", ressource = "utilisateurs")
    @GetMapping
    public ResponseEntity<List<Utilisateur>> obtenirTousLesSalaries() {
        List<Utilisateur> salaries = serviceMultiUtilisateursPaiement.listerTousLesSalaries();
        return ResponseEntity.ok(salaries);
    }
    
    //Endpoint pour basculer un droit globalement pour un rôle donné depuis l'interface
    @RequireDroit(action = "modifier")
    @PostMapping("/droits/mettre-a-jour")
    public ResponseEntity<?> mettreAJourDroitParRole(
            @RequestParam Role role,
            @RequestParam String nomDroit,
            @RequestParam boolean valeur) {
        try {
            //Appel de la méthode de l'adaptateur via votre service
            serviceMultiUtilisateursPaiement.basculerDroitPourRole(role, nomDroit, valeur);
            return ResponseEntity.ok("Droits mis à jour avec succès pour le rôle : " + role);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Erreur lors de la mise à jour des droits : " + e.getMessage());
        }
    }
  
    public static class CreateUserRequest {
        private String nom; //Changé de 'name' à 'nom' pour correspondre au front-end
        private String email;
        private String password;
        private Role role;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        
        public String getPassword() { return password; } 
        public void setPassword(String password) { this.password = password; } 
    }
    
    public static class UpdateUserRequest {
        private String nouveauNom;
        private Role nouveauRole;
        private String password; 

        public String getNouveauNom() { return nouveauNom; }
        public void setNouveauNom(String nouveauNom) { this.nouveauNom = nouveauNom; }
        
        public Role getNouveauRole() { return nouveauRole; }
        public void setNouveauRole(Role nouveauRole) { this.nouveauRole = nouveauRole; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}