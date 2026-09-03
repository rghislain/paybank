package com.paybank.hexagonal.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.paybank.hexagonal.domaine.service.GestionPaiementService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = com.paybank.hexagonal.main.PaiementApplication.class)
@ActiveProfiles("test")
@Transactional
class DroitsAopIntegrationTest_0001 {

    @Autowired
    private GestionPaiementService monServiceMetier;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUpData() {
    	/*
    	jdbcTemplate.update(
    		"INSERT INTO clients (id, nom) VALUES (?::uuid, ?::uuid) ON CONFLICT (id) DO NOTHING",
    		clientId, "Client Test"
    	);
    	*/
    	
        // Nettoyage préalable pour éviter les conflits d'unicité
        jdbcTemplate.update("DELETE FROM ressources WHERE utilisateurs_id = ?", adminId.toString());
        jdbcTemplate.update("DELETE FROM utilisateurs WHERE email = ?", "admin@paybank.com");

        // Insertion de l'utilisateur avec l'ID converti en String
        jdbcTemplate.update(
            "INSERT INTO utilisateurs (id, email, role, actif, lire, creer, modifier, supprimer, imprimer, sauvegarder) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            adminId, "admin@paybank.com", "ADMIN", true, true, true, true, true, true, true
        );

        // Insertion des ressources associées
        jdbcTemplate.update(
            "INSERT INTO ressources (id, utilisateurs_id, clients, paiements, produits) VALUES (?, ?, ?, ?, ?)",
            UUID.randomUUID(), adminId.toString(), true, true, true
        );
    }

    @Test
    @DisplayName("Vérifie qu'un utilisateur EMPLOYE (sans ressources) est bloqué")
    @WithMockUser(username = "employe@paybank.com", roles = {"EMPLOYE"})
    void testAccesRefusePourEmploye() {
        assertThrows(SecurityException.class, () -> {
            monServiceMetier.creerIntentionPaiement(UUID.randomUUID(), 99999L);
        });
    }

    @Test
    @DisplayName("Vérifie l'accès autorisé pour l'administrateur avec ressources")
    @WithMockUser(username = "admin@paybank.com", roles = {"ADMIN"})
    void testAccesAutorisePourAdmin() {
        assertDoesNotThrow(() -> {
            monServiceMetier.creerIntentionPaiement(UUID.randomUUID(), 99999L);
        });
    }
}