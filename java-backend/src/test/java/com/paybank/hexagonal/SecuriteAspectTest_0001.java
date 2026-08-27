package com.paybank.hexagonal;

import com.paybank.hexagonal.domaine.ServiceGestionPaiement;
import com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.repository.UtilisateurRepository;
import com.paybank.hexagonal.domaine.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


/*
@SpringBootTest(classes = com.paybank.hexagonal.main.PaiementApplication.class)
@EnableAspectJAutoProxy
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SecuriteAspectTest {

    @Autowired
    private ServiceGestionPaiement serviceGestionPaiement;

    @Autowired
    private ServiceMultiUtilisateursPaiement serviceMultiUtilisateurs;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @BeforeEach
    public void setUp() {
        nettoyerDonnees(); 

        SecurityContextHolder.clearContext();
        transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    jdbcTemplate.update("INSERT INTO utilisateurs (id, email, nom, role, actif) VALUES (?, ?, ?, ?, ?)",
                            "admin@paybank.com", "admin@paybank.com", "Admin System", "ADMIN", true);
                } catch (Exception e) { /* Déjà présent */ //}

                //try {
                    //jdbcTemplate.update("INSERT INTO utilisateurs (id, email, nom, role, actif) VALUES (?, ?, ?, ?, ?)",
                            //"user@paybank.com", "user@paybank.com", "Utilisateur Standard", "EMPLOYE", true);
                //} catch (Exception e) { /* Déjà présent */ }
            //}
        //});
        
        //transactionTemplate.execute(status -> {
            //entityManager.clear();
            //return null;
        //});
    //}
/*
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

    @Test
    public void testAccesAutorise_QuandRoleCorrect() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

        assertDoesNotThrow(() -> {
            serviceGestionPaiement.executerRapprochementDepuisSources();
        });
    }

    @Test
    //@WithMockUser(roles = "ADMIN")
    //@Test
    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
    public void testAntiInjection_QuandScriptXSSDetecte() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

        Utilisateur hacker = new Utilisateur();
        hacker.setId("hacker_xss");
        hacker.setName("<script>window.location='http://attaque.com'</script>");
        hacker.setEmail("hacker@paybank.com");
        hacker.setRole(Role.EMPLOYE);
        
        Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);

        assertThrows(IllegalArgumentException.class, () -> {
            serviceMultiUtilisateurs.createUser(operateurTest, hacker);
        });
    }

    @Test
    //@Test
    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
    public void testAntiInjection_QuandSqlSuspectDetecte() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

        Utilisateur hacker = new Utilisateur();
        hacker.setId("hacker_sql");
        hacker.setName("Rob");
        hacker.setEmail("test@bank.fr'; UNION SELECT null, null --");
        hacker.setRole(Role.EMPLOYE);

        Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);
        
        assertThrows(IllegalArgumentException.class, () -> {
            serviceMultiUtilisateurs.createUser(operateurTest, hacker);
        });
    }

    @Test
    //@Test
    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
    public void testAntiInjection_DonneesSaines_Valides() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

        Utilisateur utilisateurSain = new Utilisateur();
        utilisateurSain.setId("sain_user");
        utilisateurSain.setName("Alice Martin");
        utilisateurSain.setEmail("alice.martin@paybank.com");
        utilisateurSain.setRole(Role.EMPLOYE);
        
        Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);

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
*/
                
/*

                import com.paybank.hexagonal.domaine.ServiceGestionPaiement;
                import com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement;
                import com.paybank.hexagonal.domaine.Utilisateur;
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
                import org.springframework.security.test.context.support.WithMockUser;
                import org.springframework.test.annotation.DirtiesContext;
                import org.springframework.transaction.PlatformTransactionManager;
                import org.springframework.transaction.TransactionStatus;
                import org.springframework.transaction.support.TransactionCallbackWithoutResult;
                import org.springframework.transaction.support.TransactionTemplate;
                import java.util.List;
                import static org.junit.jupiter.api.Assertions.*;

                @SpringBootTest(classes = com.paybank.hexagonal.main.PaiementApplication.class)
                @EnableAspectJAutoProxy
                @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
                @Transactional
                public class SecuriteAspectTest {

                    @Autowired
                    private ServiceGestionPaiement serviceGestionPaiement;

                    @Autowired
                    private ServiceMultiUtilisateursPaiement serviceMultiUtilisateurs;

                    @Autowired
                    private JdbcTemplate jdbcTemplate;

                    @Autowired
                    private PlatformTransactionManager transactionManager;

                    @PersistenceContext
                    private EntityManager entityManager;

                    private TransactionTemplate transactionTemplate;

                    @Autowired
                    private UtilisateurRepository utilisateurRepository;

                    @BeforeEach
                    public void setUp() {
                        nettoyerDonnees(); 

                        SecurityContextHolder.clearContext();
                        transactionTemplate = new TransactionTemplate(transactionManager);

                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus status) {
                                try {
                                    jdbcTemplate.update("INSERT INTO utilisateurs (id, email, nom, role, actif) VALUES (?, ?, ?, ?, ?)",
                                            "admin@paybank.com", "admin@paybank.com", "Admin System", "ADMIN", true);
                                } catch (Exception e) { /* Déjà présent */ //}
/*
                                try {
                                    jdbcTemplate.update("INSERT INTO utilisateurs (id, email, nom, role, actif) VALUES (?, ?, ?, ?, ?)",
                                            "user@paybank.com", "user@paybank.com", "Utilisateur Standard", "EMPLOYE", true);
                                } catch (Exception e) { /* Déjà présent */ //}
                            //}
                        //});
                        /*
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

                    @Test
                    public void testAccesAutorise_QuandRoleCorrect() {
                        SecurityContextHolder.getContext()
                                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                        assertDoesNotThrow(() -> {
                            serviceGestionPaiement.executerRapprochementDepuisSources();
                        });
                    }
                    */

                    /*
                    @Test
                    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
                    public void testAntiInjection_QuandScriptXSSDetecte() {
                        SecurityContextHolder.getContext()
                                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                        Utilisateur hacker = new Utilisateur();
                        hacker.setId("hacker_xss");
                        hacker.setName("<script>window.location='http://attaque.com'</script>");
                        hacker.setEmail("hacker@paybank.com");
                        hacker.setRole(Role.EMPLOYE);
                        
                        //Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);
                        //Utilisateur operateurTest = utilisateurRepository.findById("admin@paybank.com").orElse(new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true));
                        
                        Utilisateur operateurTest = new Utilisateur();
                        operateurTest.setId("admin@paybank.com");
                        operateurTest.setEmail("admin@paybank.com");
                        operateurTest.setName("Admin System");
                        operateurTest.setRole(Role.ADMIN);
                        operateurTest.setActif(true);
                        
                        assertThrows(IllegalArgumentException.class, () -> {
                            serviceMultiUtilisateurs.createUser(operateurTest, hacker);
                        });
                    }
                    */

                    /*
                    @Test
                    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
                    public void testAntiInjection_QuandSqlSuspectDetecte() {
                        SecurityContextHolder.getContext()
                                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                        Utilisateur hacker = new Utilisateur();
                        hacker.setId("hacker_sql");
                        hacker.setName("Rob");
                        hacker.setEmail("test@bank.fr'; UNION SELECT null, null --");
                        hacker.setRole(Role.EMPLOYE);

                        //Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);
                        //Utilisateur operateurTest = utilisateurRepository.findById("admin@paybank.com")
                                //.orElse(new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true));
                        
                        Utilisateur operateurTest = new Utilisateur();
                        operateurTest.setId("admin@paybank.com");
                        operateurTest.setEmail("admin@paybank.com");
                        operateurTest.setName("Admin System");
                        operateurTest.setRole(Role.ADMIN);
                        operateurTest.setActif(true);
                        
                        assertThrows(IllegalArgumentException.class, () -> {
                            serviceMultiUtilisateurs.createUser(operateurTest, hacker);
                        });
                    }
                    */

                    /*
                    @Test
                    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
                    public void testAntiInjection_DonneesSaines_Valides() {
                        SecurityContextHolder.getContext()
                                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                        Utilisateur utilisateurSain = new Utilisateur();
                        utilisateurSain.setId("sain_user");
                        utilisateurSain.setName("Alice Martin");
                        utilisateurSain.setEmail("alice.martin@paybank.com");
                        utilisateurSain.setRole(Role.EMPLOYE);
                        
                        //Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);
                        //Utilisateur operateurTest = utilisateurRepository.findById("admin@paybank.com")
                                //.orElse(new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true));
                        
                        Utilisateur operateurTest = new Utilisateur();
                        operateurTest.setId("admin@paybank.com");
                        operateurTest.setEmail("admin@paybank.com");
                        operateurTest.setName("Admin System");
                        operateurTest.setRole(Role.ADMIN);
                        operateurTest.setActif(true);
                        
                        assertDoesNotThrow(() -> {
                            serviceMultiUtilisateurs.createUser(operateurTest, utilisateurSain);
                        });
                    }
                    */
                    
                    /*
                    @Test
                    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
                    public void testAntiInjection_QuandScriptXSSDetecte() {
                        SecurityContextHolder.getContext()
                                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                        Utilisateur hacker = new Utilisateur();
                        hacker.setId("hacker_xss");
                        hacker.setName("<script>window.location='http://attaque.com'</script>");
                        hacker.setEmail("hacker@paybank.com");
                        hacker.setRole(Role.EMPLOYE);
                        
                        // Récupération directe ou création complète avec le rôle typé
                        /*
                        Utilisateur operateurTest = new Utilisateur();
                        operateurTest.setId("admin@paybank.com");
                        operateurTest.setEmail("admin@paybank.com");
                        operateurTest.setName("Admin System");
                        operateurTest.setRole(Role.ADMIN);
                        operateurTest.setActif(true);
                        */

                        //Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);
                        //utilisateurRepository.save(operateurTest);
                        
                        //assertThrows(IllegalArgumentException.class, () -> {
                            //serviceMultiUtilisateurs.createUser(operateurTest, hacker);
                        //});
                    //}

                    /*
                    @Test
                    @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
                    public void testAntiInjection_QuandSqlSuspectDetecte() {
                        SecurityContextHolder.getContext()
                                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                        Utilisateur hacker = new Utilisateur();
                        hacker.setId("hacker_sql");
                        hacker.setName("Rob");
                        hacker.setEmail("test@bank.fr'; UNION SELECT null, null --");
                        hacker.setRole(Role.EMPLOYE);

                        /*
                        Utilisateur operateurTest = new Utilisateur();
                        operateurTest.setId("admin@paybank.com");
                        operateurTest.setEmail("admin@paybank.com");
                        operateurTest.setName("Admin System");
                        operateurTest.setRole(Role.ADMIN);
                        operateurTest.setActif(true);
                        */
                        
                    /*
                        Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);
                        utilisateurRepository.save(operateurTest);

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
                        utilisateurSain.setName("Alice Martin");
                        utilisateurSain.setEmail("alice.martin@paybank.com");
                        utilisateurSain.setRole(Role.EMPLOYE);
                        
                        /*
                        Utilisateur operateurTest = new Utilisateur();
                        operateurTest.setId("admin@paybank.com");
                        operateurTest.setEmail("admin@paybank.com");
                        operateurTest.setName("Admin System");
                        operateurTest.setRole(Role.ADMIN);
                        operateurTest.setActif(true);
                        */
                        /*
                        Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);
                        utilisateurRepository.save(operateurTest);

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
                */
                    
                   

                    import com.paybank.hexagonal.domaine.ServiceGestionPaiement;
                    import com.paybank.hexagonal.domaine.ServiceMultiUtilisateursPaiement;
                    import com.paybank.hexagonal.domaine.Utilisateur;
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
                    import org.springframework.security.test.context.support.WithMockUser;
                    import org.springframework.test.annotation.DirtiesContext;
                    import org.springframework.transaction.PlatformTransactionManager;
                    import org.springframework.transaction.TransactionStatus;
                    import org.springframework.transaction.support.TransactionCallbackWithoutResult;
                    import org.springframework.transaction.support.TransactionTemplate;
                    import java.util.List;
                    import static org.junit.jupiter.api.Assertions.*;

                    @SpringBootTest(classes = com.paybank.hexagonal.main.PaiementApplication.class)
                    @EnableAspectJAutoProxy
                    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
                    @Transactional
                    public class SecuriteAspectTest_0001 {

                        @Autowired
                        private ServiceGestionPaiement serviceGestionPaiement;

                        @Autowired
                        private ServiceMultiUtilisateursPaiement serviceMultiUtilisateurs;

                        @Autowired
                        private JdbcTemplate jdbcTemplate;

                        @Autowired
                        private PlatformTransactionManager transactionManager;

                        @PersistenceContext
                        private EntityManager entityManager;

                        private TransactionTemplate transactionTemplate;

                        @Autowired
                        private UtilisateurRepository utilisateurRepository;

                        @BeforeEach
                        public void setUp() {
                            nettoyerDonnees(); 

                            SecurityContextHolder.clearContext();
                            transactionTemplate = new TransactionTemplate(transactionManager);

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

                        @Test
                        public void testAccesAutorise_QuandRoleCorrect() {
                            SecurityContextHolder.getContext()
                                    .setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                            assertDoesNotThrow(() -> {
                                serviceGestionPaiement.executerRapprochementDepuisSources();
                            });
                        }

                        @Test
                        @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
                        public void testAntiInjection_QuandScriptXSSDetecte() {
                            //SecurityContextHolder.getContext()
                                    //.setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                            //List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                        	SecurityContextHolder.getContext().setAuthentication(
                        		    new UsernamePasswordAuthenticationToken(
                        		        "admin@paybank.com", 
                        		        "securePass", 
                        		        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
                        		    )
                        		);
                        	
                            Utilisateur hacker = new Utilisateur();
                            hacker.setId("hacker_xss");
                            hacker.setName("<script>window.location='http://attaque.com'</script>");
                            hacker.setEmail("hacker@paybank.com");
                            hacker.setRole(Role.EMPLOYE);
                            
                            Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);
                            
                            assertThrows(IllegalArgumentException.class, () -> {
                                serviceMultiUtilisateurs.createUser(operateurTest, hacker);
                            });
                        }

                        @Test
                        @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
                        public void testAntiInjection_QuandSqlSuspectDetecte() {
                            //SecurityContextHolder.getContext()
                                    //.setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                            //List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                        	SecurityContextHolder.getContext().setAuthentication(
                        		    new UsernamePasswordAuthenticationToken(
                        		        "admin@paybank.com", 
                        		        "securePass", 
                        		        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
                        		    )
                        		);
                        	
                            Utilisateur hacker = new Utilisateur();
                            hacker.setId("hacker_sql");
                            hacker.setName("Rob");
                            hacker.setEmail("test@bank.fr'; UNION SELECT null, null --");
                            hacker.setRole(Role.EMPLOYE);

                            Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);

                            assertThrows(IllegalArgumentException.class, () -> {
                                serviceMultiUtilisateurs.createUser(operateurTest, hacker);
                            });
                        }

                        @Test
                        @WithMockUser(roles = {"ADMIN", "MANAGER", "EMPLOYE"})
                        public void testAntiInjection_DonneesSaines_Valides() {
                            //SecurityContextHolder.getContext()
                                    //.setAuthentication(new UsernamePasswordAuthenticationToken("admin@paybank.com", "securePass",
                                            //List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))));

                        	SecurityContextHolder.getContext().setAuthentication(
                        		    new UsernamePasswordAuthenticationToken(
                        		        "admin@paybank.com", 
                        		        "securePass", 
                        		        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ADMIN"))
                        		    )
                        		);
                        	
                            Utilisateur utilisateurSain = new Utilisateur();
                            utilisateurSain.setId("sain_user");
                            utilisateurSain.setName("Alice Martin");
                            utilisateurSain.setEmail("alice.martin@paybank.com");
                            utilisateurSain.setRole(Role.EMPLOYE);
                            
                            Utilisateur operateurTest = new Utilisateur("admin@paybank.com", "admin@paybank.com", "Admin System", Role.ADMIN, true);

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
                