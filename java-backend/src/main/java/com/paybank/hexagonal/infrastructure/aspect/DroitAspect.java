package com.paybank.hexagonal.infrastructure.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.annotation.RequireDroit;
import com.paybank.hexagonal.domaine.service.OperateurCourantService;
import com.paybank.hexagonal.domaine.service.VerificationDroitsService;

/**
 * Applique automatiquement la vérification de droits à toute méthode annotée @RequireDroit,
 * AVANT son exécution. S'appuie sur :
 *  - OperateurCourantService : identité réelle (via le cookie JWT authentifié, jamais un header)
 *  - VerificationDroitsService : la même matrice que celle affichée/éditée sur bilan.html
 *
 * En cas de refus, une SecurityException est levée ; elle est transformée en réponse HTTP 403
 * par GlobalExceptionHandler (elle survient AVANT le corps de la méthode contrôleur, donc
 * un éventuel try/catch local dans la méthode ne l'intercepte pas).
 */
@Aspect
@Component
public class DroitAspect {

    private final OperateurCourantService operateurCourantService;
    private final VerificationDroitsService verificationDroitsService;

    public DroitAspect(OperateurCourantService operateurCourantService,
                        VerificationDroitsService verificationDroitsService) {
        this.operateurCourantService = operateurCourantService;
        this.verificationDroitsService = verificationDroitsService;
    }

    @Before("@annotation(requireDroit)")
    public void verifierDroit(JoinPoint joinPoint, RequireDroit requireDroit) {
        Role role = operateurCourantService.getRoleConnecte(); //lève SecurityException si non authentifié

        String action = requireDroit.action().isBlank() ? null : requireDroit.action();
        String ressource = requireDroit.ressource().isBlank() ? null : requireDroit.ressource();

        boolean autorise = verificationDroitsService.aLeDroit(role, action, ressource);

        if (!autorise) {
            StringBuilder message = new StringBuilder("Accès refusé : le rôle ").append(role).append(" n'a pas le droit");
            if (action != null) message.append(" [").append(action).append("]");
            if (ressource != null) message.append(" sur la ressource [").append(ressource).append("]");
            message.append(".");
            throw new SecurityException(message.toString());
        }
    }
}