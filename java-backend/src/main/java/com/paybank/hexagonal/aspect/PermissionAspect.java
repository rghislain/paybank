package com.paybank.hexagonal.aspect;

import com.paybank.hexagonal.annotation.SecuredPermission;
import com.paybank.hexagonal.domaine.model.Role;
import com.paybank.hexagonal.domaine.service.OperateurCourantService;
import com.paybank.hexagonal.domaine.service.PermissionService;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionAspect { 
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private OperateurCourantService operateurCourantService;

    @Around("@annotation(SecuredPermission)")
    public Object verifierPermission(ProceedingJoinPoint joinPoint) throws Throwable {        	
    	//1. Récupérer le rôle EFFECTIF de manière infalsifiable via le contexte de sécurité (JWT / Cookies)
        String roleActif;
        try {
            Role role = operateurCourantService.getRoleConnecte();
            roleActif = role != null ? role.name() : "EMPLOYE";
        } catch (Exception e) {
            roleActif = "EMPLOYE"; // Rôle par défaut si non authentifié ou erreur de contexte
        }
        
        //3. Lire les paramètres de l'annotation (@SecuredPermission) sur la méthode
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SecuredPermission securedPermission = method.getAnnotation(SecuredPermission.class);
        String ressource = securedPermission.ressource();
        String action = securedPermission.action();
        //4. Demander au service si ce rôle a le droit
        boolean aLeDroit = permissionService.verifierAutorisation(roleActif, ressource, action);
        if (!aLeDroit) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🚫 Accès refusé : Le rôle [" + roleActif + "] ne possède pas le droit [" + action + "] sur la ressource [" + ressource + "].");
        }
        //5. Si c'est bon, on laisse l'action du contrôleur s'exécuter normalement
        return joinPoint.proceed();
    }
}