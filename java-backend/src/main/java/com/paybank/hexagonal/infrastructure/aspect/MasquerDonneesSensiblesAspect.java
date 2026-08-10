package com.paybank.hexagonal.infrastructure.aspect;

import java.lang.reflect.Field;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.paybank.hexagonal.domaine.annotation.MasquerDonneesSensibles;

@Aspect
@Component
public class MasquerDonneesSensiblesAspect {
	@AfterReturning(
	        pointcut = "execution(* com.paybank.hexagonal.domaine..*(..))", 
	        returning = "resultat"
	    )
	    public void analyserEtMasquer(Object resultat) {
	        if (resultat == null) return;

	        // Parcourir les champs de l'objet retourné
	        Field[] fields = resultat.getClass().getDeclaredFields();
	        for (Field field : fields) {
	            if (field.isAnnotationPresent(MasquerDonneesSensibles.class) && field.getType() == String.class) {
	                try {
	                    field.setAccessible(true);
	                    String valeurOriginale = (String) field.get(resultat);
	                    if (valeurOriginale != null) {
	                        // Remplace toute la chaîne par des astérisques
	                        field.set(resultat, "****"); 
	                    }
	                } catch (IllegalAccessException e) {
	                    // Log ou ignorer en fonction de ta politique de gestion des erreurs
	                }
	            }
	        }
	    }
}