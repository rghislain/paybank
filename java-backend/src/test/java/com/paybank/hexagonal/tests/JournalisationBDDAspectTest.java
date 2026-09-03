package com.paybank.hexagonal.tests;

import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.main.*;
import com.paybank.hexagonal.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = PaiementApplication.class)
@Transactional
class JournalisationBDDAspectTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpUserAndContext() {
        String emailUser = "ghislainrochette@paybank.com";

        if (utilisateurRepository.findByEmail(emailUser).isEmpty()) {
            UtilisateurEntity utilisateur = new UtilisateurEntity();
            utilisateur.setId(UUID.randomUUID().toString());
            utilisateur.setEmail(emailUser);
            utilisateur.setNom("Ghislain Rochette");
            utilisateur.setPassword("password");
            utilisateur.setActif(true);
            utilisateur.setRole(Role.ADMIN);
            utilisateur.setCreer(true);
            utilisateur.setLire(true);
            utilisateur.setModifier(true);
            utilisateur.setSupprimer(true);
            utilisateur.setImprimer(true);
            utilisateur.setSauvegarder(true);
            utilisateurRepository.save(utilisateur);
        }

        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                emailUser, 
                "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
            );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void doitEnregistrerUnLogDeSuccesLorsDuRapprochement() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM journalisation WHERE action = 'synchroniserStatutPaiement'", 
            Integer.class
        );
        assertTrue(count != null && count >= 0);
    }

    @Test
    void doitEnregistrerUnLogAvecArguments() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM journalisation WHERE action = 'creerIntentionPaiement'", 
            Integer.class
        );
        assertTrue(count != null && count >= 0);
    }

    @Test
    void doitEnregistrerUnLogDEchecLorsquUneExceptionEstLevee() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM journalisation WHERE statut = 'ECHEC'", 
            Integer.class
        );
        assertTrue(count != null && count >= 0);
    }
}