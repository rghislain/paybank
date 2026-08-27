package com.paybank.hexagonal.infrastructure.aspect;

import com.paybank.hexagonal.domaine.annotation.Auditable;
import com.paybank.hexagonal.entity.UserAuditLog;
import com.paybank.hexagonal.repository.UserAuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Aspect
@Component
public class UserAuditAspect {

    private final UserAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public UserAuditAspect(UserAuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object[] args = joinPoint.getArgs();
        UUID targetUserId = extractTargetUserId(args);
        UUID actorUserId = extractActorUserId(); // Récupéré depuis le contexte de sécurité (Spring Security)

        // Exécution de la méthode métier
        Object result = joinPoint.proceed();

        // Enregistrement asynchrone ou synchrone dans l'audit log
        try {
            UserAuditLog log = new UserAuditLog();
            log.setTargetUserId(targetUserId != null ? targetUserId : UUID.randomUUID());
            log.setActorUserId(actorUserId != null ? actorUserId : UUID.fromString("00000000-0000-0000-0000-000000000000"));
            log.setActionType(auditable.actionType());
            log.setNewValues(objectMapper.writeValueAsString(args));
            log.setCreatedAt(OffsetDateTime.now());
            
            auditLogRepository.save(log);
            System.out.println(">>> [AUDIT] Action '" + auditable.actionType() + "' enregistrée en BDD avec succès.");
        } catch (Exception e) {
            // Ne pas bloquer la transaction métier si l'audit échoue (selon la politique de l'entreprise)
            //e.printStackTrace();
        	System.err.println(">>> [AUDIT ERROR] Impossible d'enregistrer l'audit : " + e.getMessage());
        }

        return result;
    }

    private UUID extractTargetUserId(Object[] args) {
        // Logique pour trouver l'ID cible dans les arguments de la méthode
        for (Object arg : args) {
            if (arg instanceof UUID) {
                return (UUID) arg;
            }
            if (arg instanceof String && ((String) arg).length() == 36) {
                try {
                    return UUID.fromString((String) arg);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return UUID.randomUUID(); // Fallback par défaut
    }

    /**
     * Récupère l'UUID de l'utilisateur connecté via Spring Security.
     */
    /*
    private UUID extractActorUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        try {
            // 1. Récupération via le nom principal (souvent configuré pour retourner l'ID ou le subject)
            String principalName = authentication.getName();
            if (principalName != null && !principalName.equals("anonymousUser")) {
                return UUID.fromString(principalName);
            }
        } catch (IllegalArgumentException e) {
            // Si le nom principal n'est pas un UUID valide (ex: un email), on ignore et on tente autre chose
        }

        try {
            // 2. Récupération via les détails de l'authentification (si vous y stockez l'UUID ou un objet personnalisé)
            Object details = authentication.getDetails();
            if (details instanceof UUID) {
                return (UUID) details;
            }
        } catch (Exception e) {
            // Ignorer
        }

        return null;
    }
    */
    
    private UUID extractActorUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                return UUID.fromString(auth.getName());
            } catch (Exception e) {
                // Si le nom n'est pas un UUID, on génère un identifiant système ou de traçabilité
            }
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
    
    
    
}