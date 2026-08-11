package com.paybank.hexagonal.domaine;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service; // Import ajouté

import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;
import com.paybank.hexagonal.domaine.annotation.Securise;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.ports.ClientSPI;
import com.paybank.hexagonal.ports.UtilisateurSPI;
import com.paybank.hexagonal.repository.UtilisateurRepository;

@Service // Annotation ajoutée pour Spring
public class ServiceMultiUtilisateursPaiement {
    
    // Remplacement de "= null" par "final"
    private final UtilisateurSPI utilisateurSPI;
    private final ClientSPI clientSPI;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public ServiceMultiUtilisateursPaiement(UtilisateurSPI utilisateurSPI, ClientSPI clientSPI) {
        this.utilisateurSPI = utilisateurSPI;
        this.clientSPI = clientSPI;
    }
    
    
    /*
    public Utilisateur authentifier(String email, String password) {
        // 1. Recherche par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("Utilisateur non trouvé"));

        // 2. Vérification du mot de passe
        // Note : En production, utilisez BCryptPasswordEncoder
        if (!utilisateur.getPassword().equals(password)) {
            throw new SecurityException("Mot de passe incorrect");
        }
        
        if (!utilisateur.isActive()) {
            throw new SecurityException("Compte inactif");
        }

        return utilisateur;
    }
    */
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /*
    public Utilisateur authentifier(String email, String password) {
        UtilisateurEntity utilisateur = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("Utilisateur non trouvé"));

        // On passe l'encodeur à la méthode métier
        if (!utilisateur.verifierMotDePasse(password, passwordEncoder)) {
            throw new SecurityException("Mot de passe incorrect");
        }
        
        return utilisateur.toDomain();
    }
    */

    // ==========================================
    // GESTION DES UTILISATEURS DE LA PLATEFORME
    // ==========================================

    /*
    @MasquerDonneesSensibles
    public Utilisateur createUser(Utilisateur operator, String email, String name, Role targetRole) {
        validatePermission(operator, Permission.USER_CREATE);
        
        Utilisateur newUser = new Utilisateur(UUID.randomUUID().toString(), email, name, targetRole);
        return utilisateurSPI.save(newUser);
    }
    */
    
    
    
    @Securise
    @MasquerDonneesSensibles
    public Utilisateur createUser(Utilisateur operator, Utilisateur utilisateur) {
    	System.out.println(">>> Entrée dans createUser pour : " + utilisateur.getNom());
        
        // Ajoutez un log pour l'opérateur
        //Utilisateur operator = getOperateurConnecte();
        //System.out.println(">>> Opérateur identifié : " + operator.getEmail());

        validatePermission(operator, Permission.USER_CREATE);
    	

        //Utilisateur operator = getOperateurConnecte();

        //validatePermission(operator, Permission.USER_CREATE);

        utilisateur.setId(UUID.randomUUID().toString());

        return utilisateurSPI.save(utilisateur);
    }
    
    @MasquerDonneesSensibles
    public Utilisateur updateUser(Utilisateur operator, String userIdToUpdate, String newName, Role newRole) {
        validatePermission(operator, Permission.USER_UPDATE);

        Utilisateur user = utilisateurSPI.findById(userIdToUpdate)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        // Empêcher un Manager de promouvoir quelqu'un au rang d'Admin
        if (operator.getRole() == Role.MANAGER && newRole == Role.ADMIN) {
            throw new SecurityException("Droits insuffisants pour attribuer le rôle ADMIN");
        }

        user.setName(newName);
        user.setRole(newRole);
        return utilisateurSPI.save(user);
    }

    @MasquerDonneesSensibles
    public void deactivateUser(Utilisateur operator, String userIdToDelete) {
        validatePermission(operator, Permission.USER_DELETE);

        Utilisateur user = utilisateurSPI.findById(userIdToDelete)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        
        user.deactivate(); // Soft delete recommandé en milieu bancaire pour l'audit historique
        utilisateurSPI.save(user);
    }

    // ==========================================
    // GESTION DES CLIENTS
    // ==========================================

    @MasquerDonneesSensibles
    public Client createClient(Utilisateur operator, String nom, String email) {
        validatePermission(operator, Permission.CLIENT_CREATE);

        Client newClient = new Client(UUID.randomUUID(), nom, email, operator.getId());
        clientSPI.sauvegarder(newClient);
        return newClient;
    }

    // ==========================================
    // MÉTHODE PRIVÉE DE VÉRIFICATION
    // ==========================================
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

        //String email = authentication.getName();

        /*
        return utilisateurSPI.findByEmail(email)
                .orElseThrow(() ->
                        new SecurityException("Opérateur non authentifié ou compte inactif"));
    	*/
        /*
        return utilisateurSPI.findByEmail(email)
                .or(() -> utilisateurSPI.findByNom(email)) // (Assurez-vous d'avoir une méthode findByNom dans votre SPI/Repository si besoin)
                .orElseThrow(() -> new SecurityException("Opérateur non authentifié ou compte inactif"));
    	*/
        //String principal = authentication.getName(); // Contient le nom ou l'email

        // 1. On essaie de le trouver par email (si c'est bien un email)
        /*
        return utilisateurSPI.findByEmail(principal)
                .orElseGet(() -> {
                    // 2. Si ce n'est pas un email (ex: c'est le nom), on récupère tous les utilisateurs 
                    // et on trouve celui dont le nom correspond
                    return utilisateurSPI.findAll().stream()
                            .filter(u -> u.getNom().equals(principal) || u.getEmail().equals(principal))
                            .findFirst()
                            .orElseThrow(() -> new SecurityException("Opérateur non authentifié ou compte inactif"));
                });
                */
        String identifiant = authentication.getName(); // Contient le nom ou l'email

        // 1. On cherche par email
        return utilisateurSPI.findByEmail(identifiant)
                // 2. Si non trouvé, on cherche par nom
                .or(() -> utilisateurSPI.findByNom(identifiant))
                .orElseThrow(() -> new SecurityException("Opérateur non authentifié ou compte inactif"));
    }
    
    /*
    public Utilisateur authentifier(String email, String password) {
        UtilisateurEntity utilisateur = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("Utilisateur non trouvé"));

        // AJOUTEZ CE LOG POUR DÉBOGUER
        System.out.println("DEBUG: Tentative login pour " + email);
        System.out.println("DEBUG: Compte actif en BDD : " + utilisateur.isActif());
        
        System.out.println("DEBUG: Le hash en BDD est : " + utilisateur.getPassword());
        boolean estValide = passwordEncoder.matches("password", utilisateur.getPassword().toString());
        System.out.println("DEBUG: Est-ce que le mot de passe 'password' match avec le hash ? " + estValide);
        
        if (!utilisateur.verifierMotDePasse(password, passwordEncoder)) {
            System.out.println("DEBUG: Mot de passe incorrect");
            throw new SecurityException("Mot de passe incorrect");
        }
        
        return utilisateur.toDomain();
    }
    */
    
    public Utilisateur authentifier(String email, String password) {
        UtilisateurEntity entity = utilisateurRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("Utilisateur non trouvé"));

        // Vérification explicite
        if (entity.getPassword() == null) {
            throw new SecurityException("Erreur interne : mot de passe corrompu en base");
        }

        boolean match = passwordEncoder.matches(password, entity.getPassword());
        
        // Log pour confirmer
        System.out.println("DEBUG: Match = " + match + " | Actif = " + entity.isActif());

        if (!match) {
            throw new SecurityException("Mot de passe incorrect");
        }
        
        if (!entity.isActif()) {
            throw new SecurityException("Compte inactif");
        }
        
        return entity.toDomain();
    }
    
}