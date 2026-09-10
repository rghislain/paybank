package com.paybank.hexagonal.infrastructure.aspect;

import com.paybank.hexagonal.configuration.SecurityInterceptor;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.domaine.annotation.Securise;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Arrays;

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
            String roleActuelSansRoles = SecurityInterceptor.getContextRole();
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
        String roleActuel = SecurityInterceptor.getContextRole();

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

}