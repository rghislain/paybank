package com.paybank.hexagonal.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.UtilisateurRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component // 👈 Indispensable pour que Spring gère ce filtre et injecte les services
public class JwtCookieFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    // 👇 Nécessaire pour récupérer le VRAI rôle de l'utilisateur depuis la BDD,
    //    au lieu de faire confiance à un header envoyé par le client (X-Auth-Role).
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String token = null;
        String activeRoleToken = null;

        // 1. Récupérer les cookies de la requête
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
                if ("activeRoleToken".equals(cookie.getName())) {
                    activeRoleToken = cookie.getValue();
                }
            }
        }

        // 2. Valider le token de connexion et injecter l'authentification si valide
        if (token != null && jwtService.isTokenValid(token)) {
            String username = jwtService.extractUsername(token);

            // On relit systématiquement le rôle réel en base à chaque requête (et non une donnée
            // mise en cache dans le JWT), pour que toute modification de rôle en base soit
            // immédiatement prise en compte, sans attendre l'expiration du token.
            Optional<UtilisateurEntity> utilisateurOpt = utilisateurRepository.findByEmail(username);

            if (utilisateurOpt.isPresent()) {
                UtilisateurEntity utilisateur = utilisateurOpt.get();

                List<SimpleGrantedAuthority> authorities = utilisateur.getRole() != null
                        ? List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()))
                        : Collections.emptyList();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 3. Rôle "actif" (sélecteur opérateur actif), validé par mot de passe côté serveur.
                //    On exige que ce token soit rattaché au MÊME utilisateur que le cookie de connexion,
                //    pour empêcher qu'un cookie activeRoleToken d'un autre compte soit réutilisé.
                if (activeRoleToken != null && jwtService.isTokenValid(activeRoleToken)) {
                    String activeRoleUsername = jwtService.extractUsername(activeRoleToken);
                    if (username.equals(activeRoleUsername)) {
                        String activeRole = jwtService.extractActiveRole(activeRoleToken);
                        if (activeRole != null && !activeRole.isBlank()) {
                            // Stocké en attribut de requête : lu par OperateurCourantService pour
                            // déterminer le rôle EFFECTIF de l'opérateur (voir getRoleConnecte()).
                            request.setAttribute("activeRole", activeRole);
                        }
                    }
                }
            }
            // Si l'utilisateur n'existe plus en base (ex: supprimé après émission du token),
            // on ne l'authentifie pas : la requête continuera en anonyme et sera rejetée
            // par les règles d'autorisation si l'endpoint est protégé.
        }

        filterChain.doFilter(request, response);
    }
}