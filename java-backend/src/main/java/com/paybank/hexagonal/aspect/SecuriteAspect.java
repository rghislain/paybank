package com.paybank.hexagonal.aspect;

import com.paybank.hexagonal.annotation.Securise;
import com.paybank.hexagonal.domaine.model.Utilisateur;
import com.paybank.hexagonal.domaine.service.OperateurCourantService;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gère :
 *  - @Securise(roles = {...}) : contrôle de rôle Spring Security (JWT/authorities),
 *    indépendant de la matrice de droits fine (role_permissions).
 *  - une vérification anti-injection basique sur MultiUtilisateursPaiementService.createUser.
 *
 * Note : ce fichier contenait auparavant une méthode verifierDroitAcces()
 * réagissant à @RequireDroit et dupliquant (en cassé, via l'ancien modèle
 * utilisateur.isCreer()/isLire()/...) ce que DroitAspect fait déjà correctement via
 * VerificationDroitsService/role_permissions. Les deux méthodes s'exécutaient sur
 * chaque appel @RequireDroit ; celle-ci bloquait systématiquement tout le monde
 * (y compris ADMIN), quel que soit le résultat de DroitAspect. Elle a été retirée :
 * @RequireDroit est désormais géré exclusivement par DroitAspect.
 */
@Aspect
@Component 
public class SecuriteAspect {
	 //Stockage simple en mémoire pour le compteur par utilisateur / IP (limite fixée à 5 pour le test)
    private final Map<String, AtomicInteger> rateLimitCounters = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 5;
    private OperateurCourantService opCourantService = null;

    public SecuriteAspect(OperateurCourantService opCourantService) {
        this.opCourantService = opCourantService;
    }
       
    @Before("@annotation(com.paybank.hexagonal.domaine.annotation.Securise)")
    public void verifierSecurite(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Securise securise = method.getAnnotation(Securise.class);
        if (securise == null) {
            return;
        }
        String[] rolesAutorises = securise.roles();

        //Si aucun rôle n'est explicitement requis, l'authentification suffit
        if (rolesAutorises.length == 0) {
            String roleActuelSansRoles = opCourantService.getRoleConnecte().toString();
            boolean authentifieViaThreadLocal = roleActuelSansRoles != null && !"ANONYMOUS".equals(roleActuelSansRoles);

            var authSansRoles = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            boolean authentifieViaSpringSecurity = authSansRoles != null && authSansRoles.isAuthenticated();

            if (authentifieViaThreadLocal || authentifieViaSpringSecurity) {
                return; //Authentifié, aucun rôle spécifique requis : accès autorisé
            }
            throw new SecurityException("Accès refusé : utilisateur non authentifié.");
        }

        //1. Récupération du rôle via le ThreadLocal (IHM)
        String roleActuel = opCourantService.getRoleConnecte().toString();

        //2. Fallback pour les tests unitaires (SecurityContextHolder)
        if (roleActuel == null || "ANONYMOUS".equals(roleActuel)) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                boolean matchAuth = auth.getAuthorities().stream()
                        .anyMatch(a -> {
                            String authority = a.getAuthority().replace("ROLE_", "").toUpperCase();
                            return Arrays.asList(rolesAutorises).contains(authority);
                        });

                if (matchAuth) {
                    return; //Autorisé par le contexte de test
                }
            }
        }
        //3. Validation pour l'IHM
        boolean autorise = Arrays.asList(rolesAutorises).contains(roleActuel);
        if (!autorise) {
            throw new SecurityException("Accès refusé : Rôle insuffisant (" + roleActuel + "). Rôles requis : " + Arrays.toString(rolesAutorises));
        }
    }

    @Before("execution(* com.paybank.hexagonal.domaine.service.MultiUtilisateursPaiementService.createUser(..)) && args(operator, utilisateur)")
    public void verifierInjection(Utilisateur operator, Utilisateur utilisateur) {
        if (utilisateur != null) {
            String nom = utilisateur.getNom();
            String email = utilisateur.getEmail();
            boolean nomSuspect = nom != null && nom.contains("<script>");
            boolean emailSuspect = email != null && email.contains("';");
            if (nomSuspect || emailSuspect) {
                throw new IllegalArgumentException("Données suspectes détectées !");
            }
        }
    }

    @Before("execution(* *..verifierStatutServeur(..))")
    public void verifierRateLimiting() {
        String userKey = opCourantService.getRoleConnecte().toString(); 
        if (userKey == null) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            userKey = (auth != null && auth.getName() != null) ? auth.getName() : "anonymous";
        }

        AtomicInteger compteur = rateLimitCounters.computeIfAbsent(userKey, k -> new AtomicInteger(0));
        int requetesActuelles = compteur.incrementAndGet();

        if (requetesActuelles > MAX_REQUESTS) {
            throw new RuntimeException("Rate limit dépassé : trop de requêtes.");
        }
    }
    
    public void resetCounters() {
        rateLimitCounters.clear();
    }

}