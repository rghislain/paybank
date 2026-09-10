package com.paybank.hexagonal.domaine.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.UtilisateurRepository;

/**
 * Source unique pour savoir "qui appelle réellement" un endpoint, et avec QUEL rôle EFFECTIF
 * (rôle réel du compte, sauf si un rôle actif a été validé par mot de passe via le sélecteur
 * "opérateur actif" — auquel cas ce rôle actif prime, exactement comme un "sudo -u").
 *
 * Toujours utiliser ce service (ou directement SecurityContextHolder) pour connaître
 * l'identité/le rôle de l'opérateur — jamais un header HTTP comme "X-Auth-Role", qui est
 * entièrement contrôlé par le client et donc falsifiable. Le rôle actif, lui, EST fiable :
 * il est signé côté serveur (voir JwtService.generateRoleToken) et vérifié à chaque requête
 *    par JwtCookieFilter, qui garantit qu'il est bien rattaché au même compte que le cookie
 *    de connexion.
 */
@Service
public class OperateurCourantService {

    private final UtilisateurRepository utilisateurRepository;

    public OperateurCourantService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /** Retourne l'entité JPA du compte réellement authentifié (rôle RÉEL, pas le rôle actif). */
    public UtilisateurEntity getEntiteConnectee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("Utilisateur non authentifié.");
        }

        String email = authentication.getName();

        UtilisateurEntity entity = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Opérateur introuvable en base : " + email));

        if (!entity.isActif()) {
            throw new SecurityException("Compte opérateur inactif.");
        }

        return entity;
    }

    /**
     * Retourne l'objet du domaine Utilisateur pour l'opérateur authentifié, avec son rôle
     * EFFECTIF (rôle actif validé s'il existe, sinon rôle réel du compte).
     */
    public Utilisateur getOperateurConnecte() {
        UtilisateurEntity entity = getEntiteConnectee();
        Role roleEffectif = resoudreRoleEffectif(entity.getRole());
        return new Utilisateur(entity.getNom(), entity.getEmail(), entity.getPassword(), roleEffectif, entity.isActif());
    }

    /** Retourne le rôle EFFECTIF de l'opérateur (rôle actif validé s'il existe, sinon rôle réel). */
    public Role getRoleConnecte() {
        UtilisateurEntity entity = getEntiteConnectee();
        return resoudreRoleEffectif(entity.getRole());
    }

    /** Retourne le rôle RÉEL du compte, en ignorant tout rôle actif éventuellement validé. */
    public Role getRoleReel() {
        return getEntiteConnectee().getRole();
    }

    private Role resoudreRoleEffectif(Role roleReel) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            Object activeRoleAttr = attributes.getRequest().getAttribute("activeRole");
            if (activeRoleAttr != null) {
                try {
                    return Role.valueOf(activeRoleAttr.toString().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    // Valeur inattendue : on retombe sur le rôle réel par sécurité.
                }
            }
        }

        return roleReel;
    }
}