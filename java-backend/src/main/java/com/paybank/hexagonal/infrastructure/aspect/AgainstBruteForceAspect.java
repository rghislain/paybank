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

import com.paybank.hexagonal.domaine.annotation.AgainstBruteForce;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.paybank.hexagonal.domaine.annotation.AgainstBruteForce;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/*
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
*/

/*
@Aspect
@Component
public class AgainstBruteForceAspect {

    // Stockage simple des compteurs par nom de méthode
    private final Map<String, AtomicInteger> compteurs = new ConcurrentHashMap<>();
    private final Map<String, Long> dernierReset = new ConcurrentHashMap<>();

    @Before("@annotation(com.paybank.hexagonal.domaine.annotation.AgainstBruteForce)")
    public void verifierRateLimit(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AgainstBruteForce annotation = method.getAnnotation(AgainstBruteForce.class);

        if (annotation == null) {
            return;
        }

        String methodName = method.toGenericString();
        int maxRequetes = annotation.requetesMax();
        long fenetreTempsMillis = annotation.secondes() * 1000L;

        long maintenant = System.currentTimeMillis();
        dernierReset.putIfAbsent(methodName, maintenant);
        compteurs.putIfAbsent(methodName, new AtomicInteger(0));

        // Réinitialisation de la fenêtre si le temps est écoulé
        if (maintenant - dernierReset.get(methodName) > fenetreTempsMillis) {
            compteurs.get(methodName).set(0);
            dernierReset.put(methodName, maintenant);
        }

        int requetesActuelles = compteurs.get(methodName).incrementAndGet();

        if (requetesActuelles > maxRequetes) {
            throw new RuntimeException("Rate limit dépassé : Trop de requêtes en peu de temps.");
        }
    }
}
*/

/*
@Aspect
@Component
public class AgainstBruteForceAspect {

    private final Map<String, AtomicInteger> compteurs = new ConcurrentHashMap<>();

    @Before("@annotation(com.paybank.hexagonal.domaine.annotation.AgainstBruteForce)")
    public void verifierLimite(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AgainstBruteForce annotation = method.getAnnotation(AgainstBruteForce.class);

        if (annotation == null) {
            return;
        }

        String cleMethod = method.toGenericString();
        int maxRequetes = annotation.requetesMax();

        compteurs.putIfAbsent(cleMethod, new AtomicInteger(0));
        int requetesActuelles = compteurs.get(cleMethod).incrementAndGet();

        if (requetesActuelles > maxRequetes) {
            throw new RuntimeException("Rate limit atteint : Trop de requêtes.");
        }
    }
}
*/

/*
import com.paybank.hexagonal.domaine.annotation.AgainstBruteForce;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class AgainstBruteForceAspect {

    private final Map<String, AtomicInteger> compteurs = new ConcurrentHashMap<>();

    @Before("@annotation(com.paybank.hexagonal.domaine.annotation.AgainstBruteForce)")
    public void verifierRateLimit(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AgainstBruteForce annotation = method.getAnnotation(AgainstBruteForce.class);

        if (annotation == null) {
            return;
        }

        String methodName = method.getName();
        int maxRequetes = annotation.requetesMax();

        compteurs.putIfAbsent(methodName, new AtomicInteger(0));
        int requetesActuelles = compteurs.get(methodName).incrementAndGet();

        if (requetesActuelles > maxRequetes) {
            throw new RuntimeException("Rate limit atteint : Trop de requêtes.");
        }
    }
}
*/

import com.paybank.hexagonal.domaine.annotation.AgainstBruteForce;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class AgainstBruteForceAspect {

    private final Map<String, AtomicInteger> compteurs = new ConcurrentHashMap<>();

    @Before("@annotation(com.paybank.hexagonal.domaine.annotation.AgainstBruteForce)")
    public void verifierRateLimit(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AgainstBruteForce annotation = method.getAnnotation(AgainstBruteForce.class);

        if (annotation == null) {
            return;
        }

        String cleMethod = method.getDeclaringClass().getName() + "." + method.getName();
        int maxRequetes = annotation.requetesMax();

        compteurs.putIfAbsent(cleMethod, new AtomicInteger(0));
        int requetesActuelles = compteurs.get(cleMethod).incrementAndGet();

        if (requetesActuelles > maxRequetes) {
            throw new RuntimeException("Rate limit dépassé : Trop de requêtes.");
        }
    }
}
