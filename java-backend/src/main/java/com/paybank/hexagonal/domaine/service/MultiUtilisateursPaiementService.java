package com.paybank.hexagonal.domaine.service;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.paybank.hexagonal.domaine.Client;
import com.paybank.hexagonal.domaine.Permission;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.domaine.annotation.SecuredPermission;
import com.paybank.hexagonal.domaine.annotation.Securise;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.port.ClientSPI;
import com.paybank.hexagonal.port.UtilisateurSPI;
import com.paybank.hexagonal.repository.UtilisateurRepository;

@Service //Annotation ajoutée pour Spring
public class MultiUtilisateursPaiementService {
    
    //Remplacement de "= null" par "final"
    private final UtilisateurSPI utilisateurSPI;
    private final ClientSPI clientSPI;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.passwords.admin:adminPass1}")
    private String passwordAdminStandard;

    @Value("${app.passwords.manager:managerPass1}")
    private String passwordManagerStandard;

    @Value("${app.passwords.employe:employePass1}")
    private String passwordEmployeStandard;
      
    public MultiUtilisateursPaiementService(UtilisateurSPI utilisateurSPI, ClientSPI clientSPI) {
        this.utilisateurSPI = utilisateurSPI;
        this.clientSPI = clientSPI;
    }
    
    @Securise
    @MasquerDonneesSensibles
    public Utilisateur createUser(Utilisateur operator, Utilisateur utilisateur) {
    	System.out.println(">>> Entrée dans createUser pour : " + utilisateur.getNom());     
        utilisateur.setId(UUID.randomUUID().toString());
        return utilisateurSPI.save(utilisateur);
    }
    
    @MasquerDonneesSensibles
    public Utilisateur updateUser(Utilisateur operator, String userIdToUpdate, String newName, Role newRole, String newPassword) {
        Utilisateur user = utilisateurSPI.findById(userIdToUpdate)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));       
        //Empêcher un Manager de promouvoir quelqu'un au rang d'Admin
        if (operator.getRole() == Role.MANAGER && newRole == Role.ADMIN) {
            throw new SecurityException("Droits insuffisants pour attribuer le rôle ADMIN");
        }
        
        //SÉCURITÉ : Vérification du mot de passe standard du rôle cible
        if (!verifierMotDePasseStandard(newRole, newPassword)) {
            throw new SecurityException("Mot de passe standard incorrect pour le rôle : " + newRole);
        }

        user.setNom(newName);
        user.setRole(newRole);
        
        if (newPassword != null && !newPassword.isBlank()) {
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
        }
        else {
        	user.setPassword(passwordEncoder.encode(newPassword));
        }       
        return utilisateurSPI.save(user);
    }

    @MasquerDonneesSensibles
    public void deactivateUser(Utilisateur operator, String userIdToDelete) {
        Utilisateur user = utilisateurSPI.findById(userIdToDelete)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        user.deactivate(); //Soft delete recommandé en milieu bancaire pour l'audit historique
        utilisateurSPI.save(user);
    }

    @MasquerDonneesSensibles
    public Client createClient(Utilisateur operator, String nom, String email) {
        Client newClient = new Client(UUID.randomUUID(), nom, email, operator.getId());
        clientSPI.sauvegarder(newClient);
        return newClient;
    }
    
    @MasquerDonneesSensibles
    private void validatePermission(Utilisateur operator, Permission permission) {
        if (operator == null || !operator.isActive()) {
            throw new SecurityException("Opérateur non authentifié ou compte inactif");
        }
        if (!operator.getRole().hasPermission(permission)) {
            throw new SecurityException("Accès refusé : Droits insuffisants pour l'action " + permission);
        }
    }
    
    private Utilisateur getOperateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Utilisateur non authentifié");
        }
        String identifiant = authentication.getName(); //Contient le nom ou l'email
        //1. On cherche par email
        return utilisateurSPI.findByEmail(identifiant)
                //2. Si non trouvé, on cherche par nom
                .or(() -> utilisateurSPI.findByNom(identifiant))
                .orElseThrow(() -> new SecurityException("Opérateur non authentifié ou compte inactif"));
    }
      
    public Utilisateur authentifier(String email, String password) {
        UtilisateurEntity entity = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("Utilisateur non trouvé"));
        //Vérification explicite
        if (entity.getPassword() == null) {
            throw new SecurityException("Erreur interne : mot de passe corrompu en base");
        }

        boolean match = passwordEncoder.matches(password, entity.getPassword());
        
        //Log pour confirmer
        System.out.println("DEBUG: Match = " + match + " | Actif = " + entity.isActif());

        if (!match) {
            throw new SecurityException("Mot de passe incorrect");
        }
        
        if (!entity.isActif()) {
            throw new SecurityException("Compte inactif");
        }
        Utilisateur utilisateur=new Utilisateur(entity.getPassword(), entity.getEmail(), entity.getPassword(), entity.getRole(), entity.isActif()); 
        return utilisateur;
    }
    
    public List<Utilisateur> listerTousLesSalaries() {
        return utilisateurSPI.listerTousLesSalaries();
    }
    
    private boolean verifierMotDePasseStandard(Role role, String password) {
        if (password == null) return false;
        
        switch (role) {
            case ADMIN:
                return password.equals(passwordAdminStandard);
            case MANAGER:
                return password.equals(passwordManagerStandard);
            case EMPLOYE:
                return password.equals(passwordEmployeStandard);
            default:
                return false;
        }
    }

    public void basculerDroitPourRole(Role role, String nomDroit, boolean valeur) {
        //On délègue l'action au port (SPI) qui va exécuter la mise à jour en base de données
        utilisateurSPI.basculerDroitPourRole(role, nomDroit, valeur);
    }
	 
}