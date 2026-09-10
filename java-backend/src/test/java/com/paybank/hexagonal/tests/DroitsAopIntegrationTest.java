package com.paybank.hexagonal.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import com.paybank.hexagonal.domaine.service.GestionPaiementService;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = com.paybank.hexagonal.main.PaiementApplication.class)
@Transactional
class DroitsAopIntegrationTest {

    @Autowired
    private GestionPaiementService monServiceMetier;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final UUID adminId = UUID.randomUUID();
    private final UUID employeId = UUID.randomUUID();
    private final UUID clientId = UUID.randomUUID();

    @BeforeEach
    void setUpData() {
        //Nettoyage préalable pour éviter les conflits
        jdbcTemplate.update("DELETE FROM utilisateurs WHERE email IN (?, ?)", "admin@paybank.com", "employe@paybank.com");
        jdbcTemplate.update("DELETE FROM clients WHERE id = ?::uuid", clientId);

        //Insertion d'un client valide
        jdbcTemplate.update(
            "INSERT INTO clients (id, nom, email) VALUES (?::uuid, ?, ?)",
            clientId, "Client Test AOP", "client.test@paybank.com"
        );

        //Insertion de l'utilisateur administrateur
        jdbcTemplate.update(
            "INSERT INTO utilisateurs (id, email, role, actif) VALUES (?, ?, ?, ?)",
            adminId, "admin@paybank.com", "ADMIN", true
        );
      
        //Insertion d'un utilisateur employe (sans droits de création ou sans ressources selon vos règles métier)
        jdbcTemplate.update(
            "INSERT INTO utilisateurs (id, email, role, actif) VALUES (?, ?, ?, ?)",
            employeId, "employe@paybank.com", "EMPLOYE", true
        );
    }

    @Test
    @DisplayName("Vérifie qu'un utilisateur EMPLOYE (sans droits de création) est bloqué")
    @WithMockUser(username = "employe@paybank.com", roles = {"EMPLOYE"})
    void testAccesRefusePourEmploye() {
        assertThrows(SecurityException.class, () -> {
            monServiceMetier.creerIntentionPaiement(clientId, 99999L);
        });
    }

    @Test
    @DisplayName("Vérifie l'accès autorisé pour l'administrateur avec ressources")
    @WithMockUser(username = "admin@paybank.com", roles = {"ADMIN"})
    void testAccesAutorisePourAdmin() {
        assertDoesNotThrow(() -> {
            monServiceMetier.creerIntentionPaiement(clientId, 99999L);
        });
    }

    @Test
    @DisplayName("Vérifie qu'un appel sans utilisateur authentifié déclenche une exception de sécurité")
    void testAccesSansUtilisateurAuthentifie() {
        assertThrows(Exception.class, () -> {
            monServiceMetier.creerIntentionPaiement(clientId, 1000L);
        });
    }

    @Test
    @DisplayName("Nouveau test : Vérifie qu'un utilisateur inactif est bloqué même s'il est authentifié")
    @WithMockUser(username = "inactif@paybank.com", roles = {"ADMIN"})
    void testAccesRefusePourUtilisateurInactif() {
        UUID inactifId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO utilisateurs (id, email, role, actif) VALUES (?, ?, ?, ?)",
            inactifId, "inactif@paybank.com", "ADMIN", false
        );

        assertThrows(Exception.class, () -> {
            monServiceMetier.creerIntentionPaiement(clientId, 5000L);
        });
    }
}