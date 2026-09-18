package com.paybank.hexagonal.aspect;

import java.util.regex.Pattern;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.paybank.hexagonal.domaine.model.Utilisateur;

import org.springframework.core.annotation.Order;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1) //Juste avant le traitement métier
public class AntiInjectionAspect {

    @Before("@annotation(com.paybank.hexagonal.domaine.annotation.AgainstInjection) || @annotation(com.paybank.hexagonal.domaine.annotation.Securise)")
    public void verifierInjection(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof String) {
                String valeur = (String) arg;
                if (valeur.contains("<script>") || valeur.toLowerCase().contains("drop table") || valeur.toLowerCase().contains("or 1=1")) {
                    throw new IllegalArgumentException("Tentative d'injection détectée");
                }
            }
        }
    }
    
    @Before("execution(* com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement.createUser(..)) && args(utilisateur)")
    public void verifierInjection(Utilisateur utilisateur) {
        if (utilisateur.getNom().contains("<script>") || utilisateur.getEmail().contains("';")) {
            throw new IllegalArgumentException("Données suspectes détectées !");
        }
    }   
}