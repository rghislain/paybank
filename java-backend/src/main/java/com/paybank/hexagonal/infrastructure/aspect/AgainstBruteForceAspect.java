package com.paybank.hexagonal.infrastructure.aspect;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.paybank.hexagonal.domaine.annotation.AgainstBruteForce;


@Aspect
@Component
@Order(0) // Doit s'exécuter TOUT au début (avant même la sécurité de rôle)
public class AgainstBruteForceAspect {

    // Cache thread-safe pour stocker l'historique des appels (IP/Utilisateur -> Timestamps)
    private final Map<String, List<Instant>> historiqueAppels = new ConcurrentHashMap<>();

    @Before("@annotation(rateLimited)")
    public void verifierLimite(JoinPoint joinPoint, AgainstBruteForce rateLimited) {
        // En situation réelle, récupère l'IP de la requête ou l'identifiant utilisateur
        String identifiantClient = "IP_OU_USER_ID"; 
        Instant maintenant = Instant.now();
        
        historiqueAppels.putIfAbsent(identifiantClient, new CopyOnWriteArrayList<>());
        List<Instant> timestamps = historiqueAppels.get(identifiantClient);

        // Nettoyer les appels obsolètes
        timestamps.removeIf(t -> Duration.between(t, maintenant).getSeconds() > rateLimited.secondes());

        // Vérifier si la limite est dépassée
        if (timestamps.size() >= rateLimited.requetesMax()) {
            throw new AccessDeniedException("Trop de requêtes. Veuillez réessayer plus tard.");
        }

        timestamps.add(maintenant);
    }
}