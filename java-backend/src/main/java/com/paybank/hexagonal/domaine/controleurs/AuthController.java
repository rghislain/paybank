package com.paybank.hexagonal.domaine.controleurs;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement;
import com.paybank.hexagonal.domaine.Utilisateur;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // On injecte uniquement le service du domaine
    @Autowired
    private ServiceMultiUtilisateursPaiement authService;

    /*
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            // 1. Appel de la logique métier (Domaine)
            Utilisateur user = authService.authentifier(email, password);
            
            // 2. Si authentification réussie, on génère une réponse JSON
            // Note : L'email dans le token est une solution temporaire pour votre POC
            return ResponseEntity.ok(Collections.singletonMap("token", "TOKEN_" + user.getEmail()));
            
        } catch (SecurityException e) {
            // 3. Gestion propre des erreurs de sécurité (401 Unauthorized)
            return ResponseEntity.status(401).body(Collections.singletonMap("message", e.getMessage()));
        }
    }
    */
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            // 1. Authentification métier
            Utilisateur user = authService.authentifier(email, password);
            
            // 2. CRUCIAL : Remplir le contexte de sécurité manuellement
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                email, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            return ResponseEntity.ok(Collections.singletonMap("token", "TOKEN_" + email));
                
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", e.getMessage()));
        }
    }
    
}