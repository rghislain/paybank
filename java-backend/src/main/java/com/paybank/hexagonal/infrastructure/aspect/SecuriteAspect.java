package com.paybank.hexagonal.infrastructure.aspect;

import com.paybank.hexagonal.adaptateur.SupabaseUtilisateursAdaptateur;
import com.paybank.hexagonal.configuration.ContexteSecurite;
import com.paybank.hexagonal.configuration.SecurityInterceptor;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.domaine.annotation.Securise;
import com.paybank.hexagonal.domaine.annotation.VerifierDroit;

import jakarta.annotation.PostConstruct;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Arrays;

import com.paybank.hexagonal.domaine.annotation.Securise;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.paybank.hexagonal.domaine.annotation.Securise;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Arrays;


import com.paybank.hexagonal.domaine.annotation.Securise;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Arrays;

/*
@Aspect
@Component
@Order(1) // Priorité maximale : exécuté AVANT le logging métier
public class SecuriteAspect {
    //@Before("securise)")
	@Before("@annotation(securise)")
	//@Before("@annotation(com.paybank.hexagonal.domaine.annotation.Securise)")
    public void verifierAutorisation(JoinPoint joinPoint, Securise securise) {
        // 1. Récupérer l'utilisateur connecté depuis le contexte Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Accès refusé : Utilisateur non authentifié.");
        }

        // 2. Récupérer les rôles requis par l'annotation
        String[] rolesRequis = securise.roles();
        if (rolesRequis.length == 0) {
            return; // Si aucun rôle n'est spécifié, on laisse passer (authentifié suffit)
        }

        // 3. Vérifier si l'utilisateur possède au moins un des rôles requis
        boolean aLeRole = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .anyMatch(roleUtilisateur -> Arrays.asList(rolesRequis).contains(roleUtilisateur));

        if (!aLeRole) {
            throw new AccessDeniedException("Accès refusé : Rôles requis " + Arrays.toString(rolesRequis));
        }
    }
    
    @PostConstruct
    public void init() {
        System.out.println(">>> L'aspect SecuriteAspect a été chargé par Spring !");
    }
    
    @Before("execution(* com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement.createUser(..)) && args(operator, utilisateur)")
    public void verifierInjection(Utilisateur operator, Utilisateur utilisateur) {
        if (utilisateur.getNom().contains("<script>") || utilisateur.getEmail().contains("';")) {
            throw new IllegalArgumentException("Données suspectes détectées !");
        }
    }
    
}
*/


/*
@Aspect
@Component
public class SecuriteAspect {

    @Before("@annotation(com.paybank.hexagonal.domaine.annotation.Securise)")
    public void verifierSecurite(JoinPoint joinPoint) {
        // 1. Récupérer la méthode annotée
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. Récupérer l'annotation @Securise et les rôles autorisés
        Securise securise = method.getAnnotation(Securise.class);
        if (securise == null) {
            return;
        }
        String[] rolesAutorises = securise.roles();

        // 3. Récupérer le rôle actuel de l'utilisateur via notre SecurityInterceptor (ThreadLocal)
        String roleActuel = SecurityInterceptor.getContextRole();

        // 4. Fallback de secours si le header HTTP n'est pas présent (utile pour vos tests unitaires)
        if ("ANONYMOUS".equals(roleActuel)) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                boolean matchAuth = auth.getAuthorities().stream()
                        .anyMatch(a -> Arrays.asList(rolesAutorises).contains(a.getAuthority().replace("ROLE_", "")));
                if (matchAuth) {
                    roleActuel = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
                }
            }
        }

        // 5. Valider si le rôle actuel fait partie des rôles autorisés
        boolean autorise = Arrays.asList(rolesAutorises).contains(roleActuel);

        if (!autorise) {
            throw new SecurityException("Accès refusé : Rôle insuffisant (" + roleActuel + "). Rôles requis : " + Arrays.toString(rolesAutorises));
        }
    }
}
*/

/*
@Aspect
@Component
public class SecuriteAspect {

    @Before("@annotation(com.paybank.hexagonal.domaine.annotation.Securise)")
    public void verifierSecurite(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Securise securise = method.getAnnotation(Securise.class);
        if (securise == null) {
            return;
        }
        String[] rolesAutorises = securise.roles();

        // 1. On récupère le rôle depuis le ThreadLocal (via l'IHM)
        String roleActuel = SecurityInterceptor.getContextRole();

        // 2. Si on est en mode test unitaire (pas de header HTTP / ANONYMOUS), 
        // on va chercher dans le SecurityContextHolder de Spring Security
        if (roleActuel == null || "ANONYMOUS".equals(roleActuel)) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                // On vérifie si l'une des autorités de l'utilisateur correspond aux rôles autorisés
                boolean matchAuth = auth.getAuthorities().stream()
                        .anyMatch(a -> {
                            String authority = a.getAuthority().replace("ROLE_", "").toUpperCase();
                            return Arrays.asList(rolesAutorises).contains(authority);
                        });

                if (matchAuth) {
                    // Si ça correspond, on valide l'accès directement sans bloquer
                    return; 
                }
            }
        }

        // 3. Validation classique pour l'IHM via le ThreadLocal
        boolean autorise = Arrays.asList(rolesAutorises).contains(roleActuel);

        if (!autorise) {
            throw new SecurityException("Accès refusé : Rôle insuffisant (" + roleActuel + "). Rôles requis : " + Arrays.toString(rolesAutorises));
        }
    }
}
*/

@Aspect
@Component
public class SecuriteAspect {
	
	@Autowired
    private SupabaseUtilisateursAdaptateur utilisateursAdaptateur;

    @Before("@annotation(com.paybank.hexagonal.domaine.annotation.Securise)")
    public void verifierSecurite(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Securise securise = method.getAnnotation(Securise.class);
        if (securise == null) {
            return;
        }
        String[] rolesAutorises = securise.roles();
        
        // Si aucun rôle n'est explicitement requis, l'authentification suffit
        if (rolesAutorises.length == 0) {
            String roleActuelSansRoles = SecurityInterceptor.getContextRole();
            boolean authentifieViaThreadLocal = roleActuelSansRoles != null && !"ANONYMOUS".equals(roleActuelSansRoles);

            var authSansRoles = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            boolean authentifieViaSpringSecurity = authSansRoles != null && authSansRoles.isAuthenticated();

            if (authentifieViaThreadLocal || authentifieViaSpringSecurity) {
                return; // Authentifié, aucun rôle spécifique requis : accès autorisé
            }
            throw new SecurityException("Accès refusé : utilisateur non authentifié.");
        }
         
        // 1. Récupération du rôle via le ThreadLocal (IHM)
        String roleActuel = SecurityInterceptor.getContextRole();

        // 2. Fallback pour les tests unitaires (SecurityContextHolder)
        if (roleActuel == null || "ANONYMOUS".equals(roleActuel)) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                boolean matchAuth = auth.getAuthorities().stream()
                        .anyMatch(a -> {
                            String authority = a.getAuthority().replace("ROLE_", "").toUpperCase();
                            return Arrays.asList(rolesAutorises).contains(authority);
                        });

                if (matchAuth) {
                    return; // Autorisé par le contexte de test
                }
            }
        }

        // 3. Validation pour l'IHM
        boolean autorise = Arrays.asList(rolesAutorises).contains(roleActuel);

        if (!autorise) {
            throw new SecurityException("Accès refusé : Rôle insuffisant (" + roleActuel + "). Rôles requis : " + Arrays.toString(rolesAutorises));
        }
    }
    
    @Before("@annotation(verifierDroit)")
    public void verifierDroitAcces(JoinPoint joinPoint, VerifierDroit verifierDroit) {
        // Récupérer l'ID de l'utilisateur connecté (via votre contexte de sécurité / session)
        String utilisateurId = ContexteSecurite.getUtilisateurConnecteId();
        
        // Charger l'utilisateur pour obtenir ses droits frais en BDD
        Utilisateur utilisateur = utilisateursAdaptateur.findById(utilisateurId)
            .orElseThrow(() -> new SecurityException("Utilisateur non trouvé ou non authentifié ou n'ayant pas les droits"));

        // Vérification dynamique selon l'action demandée
        boolean estAutorise = switch (verifierDroit.action().toUpperCase()) {
            case "creer" -> utilisateur.isCreer();
            case "lire" -> utilisateur.isLire();
            case "modifier" -> utilisateur.isModifier();
            case "supprimer" -> utilisateur.isSupprimer();
            case "imprimer" -> utilisateur.isImprimer();
            case "sauvegarder" -> utilisateur.isSauvegarder();
            default -> false;
        };

        if (!estAutorise) {
            throw new SecurityException("Accès refusé : vous ne possédez pas le droit " + verifierDroit.action());
        }
    }
      
    @Before("execution(* com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement.createUser(..)) && args(operator, utilisateur)")
    public void verifierInjection(Utilisateur operator, Utilisateur utilisateur) {
        if (utilisateur.getNom().contains("<script>") || utilisateur.getEmail().contains("';")) {
            throw new IllegalArgumentException("Données suspectes détectées !");
        }
    }
}

