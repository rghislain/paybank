package com.paybank.hexagonal.tests;

import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.domaine.service.GestionPaiementService;
import com.paybank.hexagonal.domaine.service.MultiUtilisateursPaiementService;
import com.paybank.hexagonal.entity.RessourcesEntity;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.UtilisateurRepository;
import com.paybank.hexagonal.domaine.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.paybank.hexagonal.main.PaiementApplication.class)
@EnableAspectJAutoProxy
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SecuriteAspectTest {

    @Autowired
    private GestionPaiementService serviceGestionPaiement;

    @Autowired
    private MultiUtilisateursPaiementService serviceMultiUtilisateurs;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public void initTransactionTemplate(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @BeforeEach
    public void setUp() {
        nettoyerDonnees(); 

        SecurityContextHolder.clearContext();

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    jdbcTemplate.update("INSERT INTO utilisateurs (id, email, nom, role, actif) VALUES (?, ?, ?, ?, ?)",
                            "admin@paybank.com", "admin@paybank.com", "Admin System", "ADMIN", true);
                } catch (Exception e) { /* Déjà présent */ }

                try {
                    jdbcTemplate.update("INSERT INTO utilisateurs (id, email, nom, role, actif) VALUES (?, ?, ?, ?, ?)",
                            "user@paybank.com", "user@paybank.com", "Utilisateur Standard", "EMPLOYE", true);
                } catch (Exception e) { /* Déjà présent */ }
            }
        });
        
        transactionTemplate.execute(status -> {
            entityManager.clear();
            return null;
        });
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                nettoyerDonnees();
            }
        });
    }

    private void nettoyerDonnees() {
        jdbcTemplate.update(
            "DELETE FROM ressources WHERE utilisateurs_id IN " +
            "(SELECT id FROM utilisateurs WHERE email != 'ghislainrochette@paybank.com')"
        );
        jdbcTemplate.update("DELETE FROM utilisateurs WHERE email != 'ghislainrochette@paybank.com'");
    }

    @Test
    public void testAccesRefuse_QuandUtilisateurNonAuthentifie() {
        assertThrows(Exception.class, () -> {
            serviceGestionPaiement.executerRapprochementDepuisSources();
        });
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void testAccesRefuse_QuandRoleInsuffisant() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("user@paybank.com", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_EMPLOYE"), new SimpleGrantedAuthority("EMPLOYE"))));

        assertThrows(Exception.class, () -> {
            serviceGestionPaiement.executerRapprochementDepuisSources();
        });
    }

    /*
    @Test
    public void testAccesAutorise_QuandRoleCorrect() {
    	UtilisateurEntity utilisateur = new UtilisateurEntity();
    	//utilisateur.setId("rg_test@paybank.com"); // Correction : l'ID doit correspondre à l'e-mail/principal recherché
    	utilisateur.setCreer(true);
    	utilisateur.setEmail("rg_test@paybank.com");
    	utilisateur.setActif(true);
    	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    	String hashedPassword = encoder.encode("adminPass1");
    	utilisateur.setPassword(hashedPassword);
    	utilisateur.setRole(Role.ADMIN);    	
    	utilisateurRepository.save(utilisateur);
    	
    	
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(utilisateur.getEmail(), utilisateur.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));
		
    	
    	SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(
                utilisateur.getEmail(), 
                "adminPass1",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
        ));
    	
        assertDoesNotThrow(() -> {
            serviceGestionPaiement.executerRapprochementDepuisSources();
        });
    }
	*/
    
    /*
    @Test
    public void testAccesAutorise_QuandRoleCorrect() {
        UtilisateurEntity utilisateur = new UtilisateurEntity();
        utilisateur.setEmail("rg_test@paybank.com");
        utilisateur.setCreer(true);
        utilisateur.setLire(true);
        utilisateur.setModifier(true);
        utilisateur.setSupprimer(true);
        utilisateur.setActif(true);
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        utilisateur.setPassword(encoder.encode("adminPass1"));
        utilisateur.setRole(Role.ADMIN);        

        // Associer les ressources et permissions spécifiques requises par l'aspect
        RessourcesEntity ressources = new RessourcesEntity();
        ressources.setUtilisateur(utilisateur);
        ressources.setPaiements(true);
        ressources.setClients(true);
        ressources.setProduits(true);
        ressources.setRapportsFinanciers(true);
        ressources.setParametresSystemes(true);
        ressources.setUtilisateurs(true);
        
        List<RessourcesEntity> liste=new ArrayList<RessourcesEntity>();
        liste.add(ressources);
        utilisateur.setRessources(liste); // Assurez-vous que le cascade persiste les ressources si configuré, ou sauvegardez-les séparément si besoin
        
        utilisateurRepository.save(utilisateur);
        
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        utilisateur.getEmail(), 
                        "adminPass1",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
                ));

        assertDoesNotThrow(() -> {
            serviceGestionPaiement.executerRapprochementDepuisSources();
        });
    }
    */
    
    /*
    @Test
    public void testAccesAutorise_QuandRoleCorrect() {
        UtilisateurEntity utilisateur = new UtilisateurEntity();
        utilisateur.setEmail("rg_test@paybank.com");
        utilisateur.setCreer(true);
        utilisateur.setLire(true);
        utilisateur.setModifier(true);
        utilisateur.setSupprimer(true);
        utilisateur.setActif(true);
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        utilisateur.setPassword(encoder.encode("adminPass1"));
        utilisateur.setRole(Role.ADMIN);        

        // Associer les ressources et permissions spécifiques requises par l'aspect
        RessourcesEntity ressources = new RessourcesEntity();
        ressources.setId(java.util.UUID.randomUUID()); // Assigner un identifiant unique manuellement si requis par l'entité
        ressources.setUtilisateur(utilisateur);
        ressources.setPaiements(true);
        ressources.setClients(true);
        ressources.setProduits(true);
        ressources.setRapportsFinanciers(true);
        ressources.setParametresSystemes(true);
        ressources.setUtilisateurs(true);
        
        List<RessourcesEntity> liste=new ArrayList<RessourcesEntity>();
        liste.add(ressources);
        utilisateur.setRessources(liste); // Assurez-vous que le cascade persiste les ressources si configuré, ou sauvegardez-les séparément si besoin
        
        utilisateur.setRessources(liste);
        
        utilisateurRepository.save(utilisateur);
        
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        utilisateur.getEmail(), 
                        "adminPass1",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
                ));

        assertDoesNotThrow(() -> {
            serviceGestionPaiement.executerRapprochementDepuisSources();
        });
    }
    */
    
    /*
    @Test
    public void testAccesAutorise_QuandRoleCorrect() {
        UtilisateurEntity utilisateur = new UtilisateurEntity();
        utilisateur.setId("rg_test@paybank.com"); // L'ID doit correspondre au principal (email) si l'aspect recherche par ID
        utilisateur.setEmail("rg_test@paybank.com");
        utilisateur.setCreer(true);
        utilisateur.setLire(true);
        utilisateur.setModifier(true);
        utilisateur.setSupprimer(true);
        utilisateur.setActif(true);
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        utilisateur.setPassword(encoder.encode("adminPass1"));
        utilisateur.setRole(Role.ADMIN);        

        RessourcesEntity ressources = new RessourcesEntity();
        ressources.setId(UUID.randomUUID());
        ressources.setUtilisateur(utilisateur);
        ressources.setPaiements(true);
        ressources.setClients(true);
        ressources.setProduits(true);
        ressources.setRapportsFinanciers(true);
        ressources.setParametresSystemes(true);
        ressources.setUtilisateurs(true);
        
        List<RessourcesEntity> liste = new ArrayList<>();
        liste.add(ressources);
        utilisateur.setRessources(liste);
        
        utilisateurRepository.save(utilisateur);
        
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        utilisateur.getEmail(), 
                        "adminPass1",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
                ));

        assertDoesNotThrow(() -> {
            serviceGestionPaiement.executerRapprochementDepuisSources();
        });
    }
    */

    @Test
    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
    public void testAntiInjection_QuandScriptXSSDetecte() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

        Utilisateur hacker = new Utilisateur();
        hacker.setId("hacker_xss");
        hacker.setNom("<script>window.location='http://attaque.com'</script>");
        hacker.setEmail("hacker@paybank.com");
        hacker.setRole(Role.EMPLOYE);
        
        Utilisateur operateurTest = new Utilisateur();
        operateurTest.setId("admin@paybank.com");
        operateurTest.setEmail("admin@paybank.com");
        operateurTest.setNom("Admin System");
        operateurTest.setRole(Role.ADMIN);
        operateurTest.setActif(true);

        assertThrows(IllegalArgumentException.class, () -> {
            serviceMultiUtilisateurs.createUser(operateurTest, hacker);
        });
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
    public void testAntiInjection_QuandSqlSuspectDetecte() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

        Utilisateur hacker = new Utilisateur();
        hacker.setId("hacker_sql");
        hacker.setNom("Rob");
        hacker.setEmail("test@bank.fr'; UNION SELECT null, null --");
        hacker.setRole(Role.EMPLOYE);

        Utilisateur operateurTest = new Utilisateur();
        operateurTest.setId("admin@paybank.com");
        operateurTest.setEmail("admin@paybank.com");
        operateurTest.setNom("Admin System");
        operateurTest.setRole(Role.ADMIN);
        operateurTest.setActif(true);
        
        assertThrows(IllegalArgumentException.class, () -> {
            serviceMultiUtilisateurs.createUser(operateurTest, hacker);
        });
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
    public void testAntiInjection_DonneesSaines_Valides() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

        Utilisateur utilisateurSain = new Utilisateur();
        utilisateurSain.setId("sain_user");
        utilisateurSain.setNom("Alice Martin");
        utilisateurSain.setEmail("alice.martin@paybank.com");
        utilisateurSain.setRole(Role.EMPLOYE);
        
        Utilisateur operateurTest = new Utilisateur();
        operateurTest.setId("admin@paybank.com");
        operateurTest.setEmail("admin@paybank.com");
        operateurTest.setNom("Admin System");
        operateurTest.setRole(Role.ADMIN);
        operateurTest.setActif(true);

        assertDoesNotThrow(() -> {
            serviceMultiUtilisateurs.createUser(operateurTest, utilisateurSain);
        });
    }

    @Test
    public void testRateLimiting_BloqueApresLimitesAtteintes() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

        assertDoesNotThrow(() -> serviceGestionPaiement.verifierStatutServeur());
        assertDoesNotThrow(() -> serviceGestionPaiement.verifierStatutServeur());
        assertDoesNotThrow(() -> serviceGestionPaiement.verifierStatutServeur());

        assertThrows(Exception.class, () -> {
            serviceGestionPaiement.verifierStatutServeur();
        });
    }
}