package com.paybank.hexagonal.infrastructure.aspect;

import com.paybank.hexagonal.domaine.annotation.SecuredPermission;
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

    @Around("@annotation(SecuredPermission)")
    public Object verifierPermission(ProceedingJoinPoint joinPoint) throws Throwable {    
        //1. Récupérer la requête HTTP courante pour lire le rôle
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Contexte de requête introuvable.");
        }
        HttpServletRequest request = attributes.getRequest();
        //2. Extraire le rôle actif envoyé par le front (via le header X-Auth-Role)
        String roleActif = request.getHeader("X-Auth-Role");
        if (roleActif == null || roleActif.isEmpty()) {
            roleActif = "EMPLOYE"; //Rôle par défaut si non spécifié
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