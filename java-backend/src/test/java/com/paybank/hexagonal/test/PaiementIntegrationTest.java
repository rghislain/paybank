package com.paybank.hexagonal.test;

import com.paybank.hexagonal.main.PaiementApplication;
import com.paybank.hexagonal.domaine.model.TransactionPaiement;
import com.paybank.hexagonal.domaine.service.PaiementService;
import com.paybank.hexagonal.sortie.port.PaiementSPI;
import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

@SpringBootTest(classes = PaiementApplication.class)
@TestPropertySource(properties = {    
    "stripe.api.url=https://api.stripe.com/v1/charges"
})
class PaiementIntegrationTest {

    @Autowired
    private PaiementService paiementService;
    
    @Autowired
    private PaiementSPI paiementSPI;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
       
    @BeforeEach
    void setUp() {
    }
    
    private void creerClient(UUID clientID) {
        String sql = "INSERT INTO clients (id, nom, email) VALUES (?::uuid, ?, ?) ON CONFLICT (id) DO NOTHING";
        jdbcTemplate.update(sql, clientID.toString(), "Client Test", "test_" + clientID + "@paybank.com");
    }

    // --- 1. TESTS DE SUCCÈS ---

    @Test
    void test1_simulerUnPaiementReussi() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        int montantCentimes = 2500;
        String cleIdempotence = UUID.randomUUID().toString();
        
        TransactionPaiement resultat = paiementService.traiterPaiement(clientId, montantCentimes, cleIdempotence, "tok_visa");

        assertNotNull(resultat);
        assertNotNull(resultat.getId());
        assertEquals(TransactionPaiement.StatutTransaction.SUCCESS, resultat.getStatut());
    }

    @Test
    void test2_simulerPaiementMontantMinimum() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement resultat = paiementService.traiterPaiement(clientId, 50, UUID.randomUUID().toString(), "tok_visa");
        assertNotNull(resultat);
        assertEquals(TransactionPaiement.StatutTransaction.SUCCESS, resultat.getStatut());
    }

    @Test
    void test3_simulerPaiementMontantEleve() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement resultat = paiementService.traiterPaiement(clientId, 500000, UUID.randomUUID().toString(), "tok_visa");
        assertNotNull(resultat);
        assertEquals(TransactionPaiement.StatutTransaction.SUCCESS, resultat.getStatut());
    }

    // --- 2. TESTS DE REFUS STRIPE (ÉCHECS) ---

    @Test
    void test4_simulerPaiementEchoueProvisionInsuffisante() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement resultat = paiementService.traiterPaiement(clientId, 2500, UUID.randomUUID().toString(), "tok_chargeDeclinedInsufficientFunds");
        assertNotNull(resultat);
        assertEquals(TransactionPaiement.StatutTransaction.FAILED, resultat.getStatut());
    }

    @Test
    void test5_simulerPaiementEchoueCarteExpiree() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement resultat = paiementService.traiterPaiement(clientId, 1200, UUID.randomUUID().toString(), "tok_chargeDeclinedExpiredCard");
        assertNotNull(resultat);
        assertEquals(TransactionPaiement.StatutTransaction.FAILED, resultat.getStatut());
    }

    @Test
    void test6_simulerPaiementEchoueCarteIncorrecte() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement resultat = paiementService.traiterPaiement(clientId, 3000, UUID.randomUUID().toString(), "tok_incorrectNumber");
        assertNotNull(resultat);
        assertEquals(TransactionPaiement.StatutTransaction.FAILED, resultat.getStatut());
    }

    //tout cvc au choix passe : cf doc stripe
    @Test
    void test7_simulerPaiementEchoueCvcInvalide() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement resultat = paiementService.traiterPaiement(clientId, 4000, UUID.randomUUID().toString(), "tok_cvcCheckFail");
        assertNotNull(resultat);
        assertEquals(TransactionPaiement.StatutTransaction.SUCCESS, resultat.getStatut());
    }

    // --- 3. TESTS DE SÉCURITÉ & IDEMPOTENCE ---

    @Test
    void test8_simulerSecuriteIdempotenceDoubleDebit() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        String cleUnique = "CLE_IDEMPOTENCE_" + UUID.randomUUID();

        TransactionPaiement premier = paiementService.traiterPaiement(clientId, 1500, cleUnique, "tok_visa");
        TransactionPaiement deuxieme = paiementService.traiterPaiement(clientId, 1500, cleUnique, "tok_visa");

        assertNotNull(premier);
        assertNotNull(deuxieme);
        assertEquals(premier.getId(), deuxieme.getId(), "L'idempotence doit renvoyer la même transaction.");
    }

    @Test
    void test9_idempotenceAvecAppelsMultiplesConsecutifs() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        String cleUnique = "CLE_MULTI_" + UUID.randomUUID();

        TransactionPaiement t1 = paiementService.traiterPaiement(clientId, 2000, cleUnique, "tok_visa");
        TransactionPaiement t2 = paiementService.traiterPaiement(clientId, 2000, cleUnique, "tok_visa");
        TransactionPaiement t3 = paiementService.traiterPaiement(clientId, 2000, cleUnique, "tok_visa");

        assertEquals(t1.getId(), t2.getId());
        assertEquals(t2.getId(), t3.getId());
    }

    // --- 4. TESTS DE PERSISTANCE ET BDD (JDBC TEMPLATE) ---

    @Test
    void test10_verifierPersistanceMontantEnBdd() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        int montant = 7500;
        TransactionPaiement tx = paiementService.traiterPaiement(clientId, montant, UUID.randomUUID().toString(), "tok_visa");

        Integer montantBdd = jdbcTemplate.queryForObject(
                "SELECT montant_centimes FROM paiements WHERE id = ?::uuid", Integer.class, tx.getId());

        assertEquals(montant, montantBdd);
    }

    @Test
    void test11_verifierStatutSuccesEnBdd() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement tx = paiementService.traiterPaiement(clientId, 1000, UUID.randomUUID().toString(), "tok_visa");

        String statutBdd = jdbcTemplate.queryForObject(
                "SELECT statut FROM paiements WHERE id = ?::uuid", String.class, tx.getId());

        assertEquals("SUCCESS", statutBdd);
    }

    @Test
    void test12_verifierStatutEchecEnBdd() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement tx = paiementService.traiterPaiement(clientId, 1000, UUID.randomUUID().toString(), "tok_chargeDeclinedInsufficientFunds");

        String statutBdd = jdbcTemplate.queryForObject(
                "SELECT statut FROM paiements WHERE id = ?::uuid", String.class, tx.getId());

        assertEquals("FAILED", statutBdd);
    }

    @Test
    void test13_verifierPresenceClientEnBdd() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE id = ?::uuid", Integer.class, clientId);

        assertEquals(1, count);
    }

    @Test
    void test14_verifierAssociationClientPaiement() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement tx = paiementService.traiterPaiement(clientId, 3300, UUID.randomUUID().toString(), "tok_visa");

        String clientAssocieId = jdbcTemplate.queryForObject(
                "SELECT client_id FROM paiements WHERE id = ?::uuid", String.class, tx.getId());

        assertEquals(clientId.toString(), clientAssocieId);
    }

    // --- 5. TESTS DE ROBUSTESSE & CAS DIVERS ---

    @Test
    void test15_verifierUniciteIdTransaction() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        TransactionPaiement tx1 = paiementService.traiterPaiement(clientId, 1000, UUID.randomUUID().toString(), "tok_visa");
        TransactionPaiement tx2 = paiementService.traiterPaiement(clientId, 1000, UUID.randomUUID().toString(), "tok_visa");

        assertNotEquals(tx1.getId(), tx2.getId());
    }

    @Test
    void test16_traiterPlusieursPaiementsPourUnMemeClient() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        
        paiementService.traiterPaiement(clientId, 1000, UUID.randomUUID().toString(), "tok_visa");
        paiementService.traiterPaiement(clientId, 2000, UUID.randomUUID().toString(), "tok_visa");

        Integer totalPaiements = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM paiements WHERE client_id = ?::uuid", Integer.class, clientId);

        assertEquals(2, totalPaiements);
    }

    @Test
    void test17_verifierCleIdempotenceEnregistree() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        String cleUnique = "CLE_TRACE_" + UUID.randomUUID();
        TransactionPaiement tx = paiementService.traiterPaiement(clientId, 1500, cleUnique, "tok_visa");

        String cleEnBdd = jdbcTemplate.queryForObject(
                "SELECT cle_idempotence FROM paiements WHERE id = ?::uuid", String.class, tx.getId());

        assertEquals(cleUnique, cleEnBdd);
    }

    @Test
    void test18_verifierInjectabiliteSPI() {
        assertNotNull(paiementSPI, "Le port SPI doit être injecté correctement par Spring.");
    }

    @Test
    void test19_verifierInjectabiliteService() {
        assertNotNull(paiementService, "Le service métier doit être injecté correctement par Spring.");
    }

    @Test
    void test20_verificationGlobaleCycleDeViePaiement() {
        UUID clientId = UUID.randomUUID();
        this.creerClient(clientId);
        String cleIdempotence = UUID.randomUUID().toString();
        
        // Exécution
        TransactionPaiement tx = paiementService.traiterPaiement(clientId, 8800, cleIdempotence, "tok_visa");
        
        // Assertions croisées objet + BDD
        assertNotNull(tx.getId());
        assertEquals(TransactionPaiement.StatutTransaction.SUCCESS, tx.getStatut());
        
        Integer montantBdd = jdbcTemplate.queryForObject("SELECT montant_centimes FROM paiements WHERE id = ?::uuid", Integer.class, tx.getId());
        assertEquals(8800, montantBdd);
    }
}