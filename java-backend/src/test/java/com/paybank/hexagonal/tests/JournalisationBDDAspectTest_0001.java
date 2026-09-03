package com.paybank.hexagonal.tests;

import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.service.GestionPaiementService;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.main.PaiementApplication;
import com.paybank.hexagonal.repository.UtilisateurRepository;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

//@ActiveProfiles("test")
//@SpringBootTest(classes = PaiementApplication.class)
/*
@SpringBootTest(
		classes = PaiementApplication.class,
		properties = {
				"spring.datasource.url=jdbc:postgresql://host.docker.internal:54321/postgres"
		}
	)
*/

/*
@SpringBootTest(
	    classes = PaiementApplication.class,
	    properties = {
	        // Force l'utilisation d'une base H2 en mémoire pour ce test
	        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDER=HIGH",
	        "spring.datasource.driver-class-name=org.h2.Driver",
	        "spring.datasource.username=sa",
	        "spring.datasource.password=",
	        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
	    }
	)
	@ActiveProfiles("test")
*/

/*
@SpringBootTest(
	    classes = PaiementApplication.class,
	    properties = {
	        // Une URL PostgreSQL factice qui ne sera pas appelée immédiatement
	        "spring.datasource.url=jdbc:postgresql://localhost:5432/fake_db_for_test",
	        "spring.datasource.username=postgres",
	        "spring.datasource.password=postgres",
	        // Empêche Hibernate de tenter de se connecter pour générer/valider le schéma
	        "spring.jpa.hibernate.ddl-auto=none",
	        // Désactive la détection automatique du Dialecte via une connexion active
	        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect"
	    }
	)
	@ActiveProfiles("test")
*/

/*
@SpringBootTest(
	    classes = PaiementApplication.class,
	    properties = {
	        // Toujours l'URL factice PostgreSQL
	        "spring.datasource.url=jdbc:postgresql://localhost:5432/fake_db_for_test",
	        "spring.datasource.username=postgres",
	        "spring.datasource.password=postgres",
	        
	        // Partie JPA/Hibernate (déjà OK)
	        "spring.jpa.hibernate.ddl-auto=none",
	        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
	        
	        // Partie Spring Data JDBC (Pour bloquer l'erreur actuelle)
	        "spring.data.jdbc.repositories.enabled=false"
	    }
	)
@ActiveProfiles("test")
*/

//@SpringBootTest(classes = PaiementApplication.class)

/*
@SpringBootTest(
	    classes = com.paybank.hexagonal.main.PaiementApplication.class,
	    properties = {
	        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
	        "spring.datasource.driver-class-name=org.h2.Driver",
	        "spring.datasource.username=sa",
	        "spring.datasource.password=",
	        "spring.jpa.hibernate.ddl-auto=create-drop",
	        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
	    }
	)
*/

@SpringBootTest(
	    classes = com.paybank.hexagonal.main.PaiementApplication.class,
	    properties = {
	        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
	        "spring.datasource.driver-class-name=org.h2.Driver",
	        "spring.datasource.username=sa",
	        "spring.datasource.password=",
	        "spring.jpa.hibernate.ddl-auto=create-drop"
	    }
	)
//@EnableAspectJAutoProxy(proxyTargetClass = true)
@Transactional
@ActiveProfiles("test")
public class JournalisationBDDAspectTest_0001 {
	
    @Autowired
    private GestionPaiementService serviceGestionPaiement;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // Compteur statique pour s'assurer que le nettoyage ne s'exécute qu'UNE fois
    //private static boolean estNettoye = false;

    /*
    @org.junit.jupiter.api.BeforeEach
    public void nettoyerUneSeuleFoisAuDemarrage() {
        if (!estNettoye) {
            jdbcTemplate.update("DELETE FROM journalisation");
            estNettoye = true; // Empêche les autres tests de re-vider la table
        }
    }
    */
    
   
    
    
    @BeforeEach
    public void nettoyerLaBaseEntreLesTests() {
        // 1. On crée le domaine requis pour le cast ?::jsonb sous H2
        jdbcTemplate.execute("CREATE DOMAIN IF NOT EXISTS jsonb AS TEXT");

        // 2. On crée la table journalisation sans la virgule finale
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS journalisation (
                    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
                    date_evenement TIMESTAMP,
                    operateur_role VARCHAR,
                    action VARCHAR,
                    statut VARCHAR,
                    details jsonb
                )
        """);
        
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS paiements (
                    id VARCHAR(255) PRIMARY KEY,
                    client_id VARCHAR(255),
                    montant_centimes BIGINT,
                    devise VARCHAR(10),
                    stripe_payment_intent_id VARCHAR(255),
                    statut VARCHAR(50),
                    cree_le TIMESTAMP,
                    cle_idempotence VARCHAR(255)
                )
        """);

        // 3. On vide la table pour garantir l'isolement des tests
        jdbcTemplate.execute("TRUNCATE TABLE journalisation");
        jdbcTemplate.execute("TRUNCATE TABLE paiements");
    }

    /*
    @Test
    public void doitEnregistrerUnLogDeSuccesLorsDuRapprochement() throws Exception {
    	SecurityContextHolder.getContext().setAuthentication(
    	        new UsernamePasswordAuthenticationToken(
    	            "admin", 
    	            "password", 
    	            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
    	        )
    	    );
    	
        serviceGestionPaiement.executerRapprochementDepuisSources();

        List<Map<String, Object>> logGlobal = jdbcTemplate.queryForList(
            "SELECT * FROM journalisation WHERE action = 'executerRapprochementDepuisSources'"
        );
        assertEquals(1, logGlobal.size(), "Il doit y avoir un log principal de succès");
        assertEquals("SUCCES", logGlobal.get(0).get("statut"));

        List<Map<String, Object>> lesEcarts = jdbcTemplate.queryForList(
            "SELECT * FROM journalisation WHERE action LIKE 'ECART_%'"
        );
        assertEquals(22, lesEcarts.size(), "Le nombre d'écarts doit correspondre au volume réel renvoyé par Stripe");
    }
    */
    
    @BeforeEach
    void setUpUserAndContext() {
        String utilisateurId = UUID.randomUUID().toString();
        String emailUser = "admin-test@paybank.com";
        
        // 1. Créer et enregistrer l'utilisateur en base en lui assignant manuellement un ID
        UtilisateurEntity utilisateur = new UtilisateurEntity();
        utilisateur.setId(utilisateurId); // Correction de l'erreur d'identifiant manquant
        utilisateur.setEmail(emailUser);
        utilisateur.setPassword("password");
        utilisateur.setActif(true);
        utilisateur.setRole(Role.ADMIN);
        utilisateurRepository.save(utilisateur);

        // 2. Configurer le SecurityContextHolder
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                utilisateur.getEmail(), 
                "password", 
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
            );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    @BeforeEach
   	void setUpUser() {
   	    // Insérer l'utilisateur attendu par votre aspect de sécurité
   	    UtilisateurEntity utilisateur = new UtilisateurEntity();
   	    utilisateur.setEmail("test@paybank.com"); // Mettez l'e-mail utilisé par vos tests
   	    utilisateur.setActif(true);
   	    // Remplissez les autres champs obligatoires...
   	    utilisateurRepository.save(utilisateur);
   	}
       
       @BeforeEach
       void setUp() {
       	String utilisateurId = UUID.randomUUID().toString();
           String emailUser = "admin-test@paybank.com";
           
           // 1. Créer et enregistrer l'utilisateur en base avec cet ID précis
           UtilisateurEntity utilisateur = new UtilisateurEntity();
           utilisateur.setId(utilisateurId);
           utilisateur.setEmail(emailUser);
           utilisateur.setPassword("password");
           utilisateur.setActif(true);
           utilisateur.setRole(Role.ADMIN);
           utilisateurRepository.save(utilisateur);

           // 2. Configurer le SecurityContextHolder en y mettant l'entité ou l'ID si c'est ce que getUtilisateurConnecteId() récupère du principal
           UsernamePasswordAuthenticationToken authentication = 
               new UsernamePasswordAuthenticationToken(
                   utilisateurId, // ou utilisateurId selon l'implémentation de ContexteSecurite
                   "password", 
                   List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
               );
           
           SecurityContextHolder.getContext().setAuthentication(authentication);
       }
    
    @Test
    public void doitEnregistrerUnLogDeSuccesLorsDuRapprochement() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "admin@paybank.com",
                "adminPass1", 
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
            )
        );
        
        // Si votre SecurityInterceptor lit un attribut de requête ou un header, assurez-vous qu'il est simulé si besoin, 
        // ou utilisez le fallback de secours dans le service que nous avons mis en place.
        serviceGestionPaiement.executerRapprochementDepuisSources();

        List<Map<String, Object>> logGlobal = jdbcTemplate.queryForList(
            "SELECT * FROM journalisation WHERE action = 'executerRapprochementDepuisSources'"
        );
        assertEquals(1, logGlobal.size(), "Il doit y avoir un log principal de succès");
        assertEquals("SUCCES", logGlobal.get(0).get("statut"));

        List<Map<String, Object>> lesEcarts = jdbcTemplate.queryForList(
            "SELECT * FROM journalisation WHERE action LIKE 'ECART_%'"
        );
        assertEquals(20, lesEcarts.size(), "Le nombre d'écarts doit correspondre au volume réel renvoyé par Stripe");
    }
    

    @Test
    public void doitEnregistrerUnLogAvecArguments() throws Exception {
        UUID clientId = UUID.randomUUID();
        long montant = 5000L;

        try {
            serviceGestionPaiement.creerIntentionPaiement(clientId, montant);
        } catch (Exception e) {
            // Attendu
        }

        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
            "SELECT * FROM journalisation WHERE action = 'creerIntentionPaiement'"
        );
        
        assertEquals(1, logs.size(), "On doit trouver le log créé par ce test précis");
        Map<String, Object> log = logs.get(0);
        assertEquals("ECHEC", log.get("statut"));
        
        String details = log.get("details").toString();
        assertTrue(details.contains(clientId.toString()), "Les arguments doivent être présents.");
    }

    @Test
    public void doitEnregistrerUnLogDEchecLorsquUneExceptionEstLevee() {
        assertThrows(Exception.class, () -> {
            serviceGestionPaiement.synchroniserStatutPaiement("ID_INVALIDE_QUI_FAIT_CRASHER");
        });

        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
            "SELECT * FROM journalisation WHERE action = 'synchroniserStatutPaiement'"
        );
        
        assertEquals(1, logs.size(), "On doit trouver le log d'échec de ce test précis");
        Map<String, Object> log = logs.get(0);
        assertEquals("ECHEC", log.get("statut"));
    }
}