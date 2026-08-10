package com.paybank.hexagonal.infrastructure.aspect;

import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.domaine.annotation.Securise;
import jakarta.annotation.PostConstruct;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
@Order(1) // Priorité maximale : exécuté AVANT le logging métier
public class SecuriteAspect {
    //@Before("securise)")
	@Before("@annotation(securise)")
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
    
    @Before("execution(* com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement.createUser(..)) && args(utilisateur)")
    public void verifierInjection(Utilisateur utilisateur) {
        if (utilisateur.getNom().contains("<script>") || utilisateur.getEmail().contains("';")) {
            throw new IllegalArgumentException("Données suspectes détectées !");
        }
    }
    
}