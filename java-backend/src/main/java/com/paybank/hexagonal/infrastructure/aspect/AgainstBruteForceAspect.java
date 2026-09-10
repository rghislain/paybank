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
