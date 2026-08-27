package com.paybank.hexagonal.infrastructure.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.paybank.hexagonal.domaine.annotation.CheckDroit;
import com.paybank.hexagonal.entity.RessourcesEntity;
import com.paybank.hexagonal.repository.RessourcesRepository;

@Aspect
@Component
public class SecuriteDroitAspect {

    @Autowired
    private RessourcesRepository ressourcesRepository;

    @Before("@annotation(checkDroit)")
    public void verifierAccesRessource(JoinPoint joinPoint, CheckDroit checkDroit) {
        String emailUtilisateur = SecurityContextHolder.getContext().getAuthentication().getName();

        RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(emailUtilisateur)
            .orElseThrow(() -> new SecurityException("Accès refusé : aucune ressource associée."));

        boolean estAutorise = false;
        if ("clients".equalsIgnoreCase(checkDroit.ressource())) {
            estAutorise = ressource.getClients();
        } else if ("paiements".equalsIgnoreCase(checkDroit.ressource())) {
            estAutorise = ressource.getPaiements();
        }

        if (!estAutorise) {
            throw new SecurityException("Action interdite : vous n'avez pas les droits sur la ressource " + checkDroit.ressource());
        }
    }
}