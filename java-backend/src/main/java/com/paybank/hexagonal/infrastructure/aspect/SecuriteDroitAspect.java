package com.paybank.hexagonal.infrastructure.aspect;

import java.util.List;
import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.paybank.hexagonal.domaine.annotation.CheckDroit;
import com.paybank.hexagonal.entity.RessourcesEntity;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.RessourcesRepository;
import com.paybank.hexagonal.repository.UtilisateurRepository;

@Aspect
@Component
public class SecuriteDroitAspect {

    @Autowired
    private RessourcesRepository ressourcesRepository;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository; // Nécessaire pour faire la liaison e-mail -> ID

    /*
    @Before("@annotation(checkDroit)")
    public void verifierAccesRessource(JoinPoint joinPoint, CheckDroit checkDroit) {
        String emailUtilisateur = SecurityContextHolder.getContext().getAuthentication().getName();

        //RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(emailUtilisateur)
            //.orElseThrow(() -> new SecurityException("Accès refusé : aucune ressource associée. L'utilisateur n'a pas les droits"));

        // Récupération sécurisée : si aucune ligne n'existe, on récupère Optional.empty()
        RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(emailUtilisateur)
            .orElse(null);

        boolean estAutorise = false;
        
        //boolean estAutorise = false;
        
        if (ressource != null) {
	        if ("clients".equalsIgnoreCase(checkDroit.ressource())) {
	            estAutorise = Boolean.TRUE.equals(ressource.getClients()); //ressource.getClients();
	        } else if ("paiements".equalsIgnoreCase(checkDroit != null ? checkDroit.ressource() : "")) { //("paiements".equalsIgnoreCase(checkDroit.ressource())) {
	            estAutorise = Boolean.TRUE.equals(ressource.getPaiements()); //ressource.getPaiements();
	        }
        }

        if (!estAutorise) {
            throw new SecurityException("Action interdite : vous n'avez pas les droits sur la ressource " + checkDroit.ressource());
        }
    }
    */
    
    /*
    @Before("@annotation(checkDroit)")
    public void verifierAccesRessource(JoinPoint joinPoint, CheckDroit checkDroit) {
        String emailUtilisateur = SecurityContextHolder.getContext().getAuthentication().getName();

        // Récupération sécurisée : si aucune ligne n'existe, on renvoie null
        RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(emailUtilisateur)
            .orElse(null);

        boolean estAutorise = false;

        // Si une ligne de ressources existe, on évalue les droits spécifiques
        if (ressource != null) {
            String nomRessource = checkDroit.ressource();
            if ("clients".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getClients());
            } else if ("paiements".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getPaiements());
            } else if ("produits".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getProduits()); // Ajustez selon votre entité
            } else if ("rapports_financiers".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getRapportsFinanciers());
            }
        }

        // Si l'utilisateur n'a pas la ressource ou que le droit est false/null
        if (!estAutorise) {
            throw new SecurityException("Action interdite : vous n'avez pas les droits sur la ressource " + checkDroit.ressource());
        }
    }
    */
    
    /*
    @Before("@annotation(checkDroit)")
    public void verifierAccesRessource(JoinPoint joinPoint, CheckDroit checkDroit) {
        // 1. Récupérer l'e-mail ou le username stocké dans le contexte Spring Security
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Trouver l'utilisateur en BDD pour récupérer son véritable ID technique (UUID)
        UtilisateurEntity utilisateur = utilisateurRepository.findByEmail(principal)
            .orElseThrow(() -> new SecurityException("Utilisateur introuvable en base de données."));

        // 3. Interroger les ressources en utilisant l'ID technique et non l'e-mail
        RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(utilisateur.getId())
            .orElse(null);

        boolean estAutorise = false;

        // 4. Évaluer les droits si l'enregistrement existe
        if (ressource != null) {
            String nomRessource = checkDroit.ressource();
            if ("clients".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getClients());
            } else if ("paiements".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getPaiements());
            } else if ("produits".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getProduits());
            } else if ("rapports_financiers".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getRapportsFinanciers());
            } else if ("parametres_systemes".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getParametresSystemes());
            }
        }

        // 5. Bloquer si non autorisé
        if (!estAutorise) {
            throw new SecurityException("Action interdite : vous n'avez pas les droits sur la ressource " + checkDroit.ressource());
        }
    }
    */
    
    /*
    @Before("@annotation(checkDroit)")
    public void verifierAccesRessource(JoinPoint joinPoint, CheckDroit checkDroit) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔍 Principal actuel : " + (authentication != null ? authentication.getPrincipal() : "null"));
        System.out.println("🔍 Est authentifié : " + (authentication != null ? authentication.isAuthenticated() : false));
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("Accès refusé : utilisateur non authentifié.");
        }

        String principal = authentication.getName();

        // Recherche souple : on tente par e-mail, puis par nom si l'e-mail échoue
        UtilisateurEntity utilisateur = utilisateurRepository.findByEmail(principal)
            .orElseGet(() -> (utilisateurRepository.findByNom(principal))
                .orElseThrow(() -> new SecurityException("Utilisateur introuvable en base pour : " + principal)));

        // Récupération des ressources via l'ID technique
        RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(utilisateur.getId())
            .orElse(null);

        boolean estAutorise = false;

        if (ressource != null) {
            String nomRessource = checkDroit.ressource();
            if ("clients".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getClients());
            } else if ("paiements".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getPaiements());
            } else if ("produits".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getProduits());
            } else if ("rapports_financiers".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getRapportsFinanciers());
            } else if ("parametres_systemes".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE.equals(ressource.getParametresSystemes());
            }
        }

        if (!estAutorise) {
            throw new SecurityException("Action interdite : vous n'avez pas les droits sur la ressource " + checkDroit.ressource());
        }
    }
    */
    
    @Before("@annotation(checkDroit)")
    public void verifierAccesRessource(JoinPoint joinPoint, CheckDroit checkDroit) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("Accès refusé : utilisateur non authentifié.");
        }

        String principal = authentication.getName();

        UtilisateurEntity utilisateur = utilisateurRepository.findByEmail(principal)
            .orElseGet(() -> utilisateurRepository.findByNom(principal)
                .orElseThrow(() -> new SecurityException("Utilisateur introuvable en base pour : " + principal)));

        // Récupération directe via la relation @OneToOne de l'entité
        //RessourcesEntity ressource = utilisateur.getRessource();

     // Récupération sécurisée depuis la liste des ressources
        List<RessourcesEntity> ressourcesList = utilisateur.getRessources();
        RessourcesEntity ressource = (ressourcesList != null && !ressourcesList.isEmpty()) ? ressourcesList.get(0) : null;
       
        boolean estAutorise = false;

        /*
        if (ressource != null) {
            String nomRessource = checkDroit.ressource();
            if ("clients".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE;
            } 
            else if ("paiements".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE;
            } 
            else if ("produits".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE;
            } else if ("rapports_financiers".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE;
            } else if ("parametres_systemes".equalsIgnoreCase(nomRessource)) {
                estAutorise = Boolean.TRUE;
            } else if ("utilisateurs".equalsIgnoreCase(nomRessource)) {
            	estAutorise = Boolean.TRUE;
            }
        }
        */
        
        if (ressource != null) {
            String nomRessource = checkDroit.ressource();
            switch (nomRessource.toLowerCase().trim()) {
                case "clients":
                    estAutorise = ressource.getClients();
                    break;
                case "paiements":
                    estAutorise = ressource.getPaiements();
                    break;
                case "produits":
                    estAutorise = ressource.getProduits();
                    break;
                case "rapports_financiers":
                case "rapports":
                    estAutorise = ressource.getRapportsFinanciers();
                    break;
                case "parametres_systemes":
                case "parametres":
                    estAutorise = ressource.getParametresSystemes();
                    break;
                case "utilisateurs":
                    estAutorise = ressource.getUtilisateurs();
                    break;
                default:
                    estAutorise = false;
            }
        }

        if (!estAutorise) {
            throw new SecurityException("Action interdite : vous n'avez pas les droits sur la ressource " + checkDroit.ressource());
        }
    }
    
}