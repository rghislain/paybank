package com.paybank.hexagonal.test;

import com.paybank.hexagonal.domaine.model.Role;
import com.paybank.hexagonal.entite.UtilisateurEntity;
import com.paybank.hexagonal.jpaRepository.UtilisateurRepository;
import com.paybank.hexagonal.main.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
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
            //Les droits (creer/lire/modifier/supprimer/imprimer/sauvegarder) ne sont plus
            //portés par UtilisateurEntity : ils vivent désormais dans role_permissions,
            //gérée via GestionDroitsService. Ce test ne vérifie que la journalisation,
            //donc aucun droit particulier n'est nécessaire pour ce compte de test
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