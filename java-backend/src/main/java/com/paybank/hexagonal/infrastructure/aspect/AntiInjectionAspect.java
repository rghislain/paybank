package com.paybank.hexagonal.infrastructure.aspect;

import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Aspect
@Component
@Order(1) // Juste avant le traitement métier
public class AntiInjectionAspect {

    // Pattern simple détectant des balises HTML ou des mots-clés SQL suspects
    private static final Pattern PATTERN_MALVEILLANT = Pattern.compile(
        "(<script>|javascript:|UNION SELECT|OR 1=1|--|;)", 
        Pattern.CASE_INSENSITIVE
    );

    @Before("execution(* com.paybank.hexagonal.domaine..*(..))")
    public void inspecterArguments(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof String chaine) {
                if (PATTERN_MALVEILLANT.matcher(chaine).find()) {
                    throw new IllegalArgumentException("Données invalides : Caractères ou commandes interdits détectés.");
                }
            }
            // Optionnel : utiliser la réflexion pour inspecter les champs String des objets DTO complexes reçus.
        }
    }
}