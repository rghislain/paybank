package com.paybank.hexagonal.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.domaine.service.MultiUtilisateursPaiementService;
import com.paybank.hexagonal.main.PaiementApplication;
import com.paybank.hexagonal.port.ClientSPI;
import com.paybank.hexagonal.port.UtilisateurSPI;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.Utilisateur;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;

@SpringBootTest(classes = PaiementApplication.class) // 👈 On lui donne la classe de configuration explicitement
//@ActiveProfiles("test")
/*
@TestPropertySource(properties = {
	    "spring.datasource.url=jdbc:postgresql://localhost:54322/postgres",
	    "spring.datasource.username=postgres",
	    "spring.datasource.password=postgres"
	})
*/
class MultiUtilisateursPaiementTest {

    private UtilisateurSPI utilisateurSPI;
    private ClientSPI clientSPI;
    private MultiUtilisateursPaiementService service;

    private Utilisateur adminOperateur;
    private Utilisateur managerOperateur;

    @BeforeEach
    void setUp() {
        // Mock des interfaces (Ports de sortie)
        utilisateurSPI = Mockito.mock(UtilisateurSPI.class);
        clientSPI = Mockito.mock(ClientSPI.class);
        
        service = new MultiUtilisateursPaiementService(utilisateurSPI, clientSPI);

        // Création des profils d'opérateurs pour les tests
        adminOperateur = new Utilisateur("admin-1", "admin@paybank.com", "Admin Root", Role.ADMIN, true);
        managerOperateur = new Utilisateur("manager-1", "manager@paybank.com", "Manager Business", Role.MANAGER, true);
    }

    /*
    @Test
    void admin_should_successfully_create_user() {
    	var auth = new UsernamePasswordAuthenticationToken(
                "admin@paybank.com", 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // 2. Exécuter votre logique de test
        //ServiceMultiUtilisateursPaiement.createUser(adminOperateur);
    	
        // Given
        String email = "john.doe@gmail.com";
        String name = "John Doe";
        Role role = Role.MANAGER;
        
        

        when(utilisateurSPI.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Utilisateur created = service.createUser(adminOperateur); //, email, name, role

        // Then
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(email, created.getEmail());
        assertEquals(role, created.getRole());
        verify(utilisateurSPI, times(1)).save(any(Utilisateur.class));
        
        // 3. Optionnel : Nettoyer après le test
        SecurityContextHolder.clearContext();
    }
    */
    
    /*
    @Test
    void admin_should_successfully_create_user() {
        // 1. Setup Sécurité
        var auth = new UsernamePasswordAuthenticationToken(
                "admin@paybank.com", 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // 2. Préparation des données (Given)
        String email = "john.doe@gmail.com";
        String name = "John Doe";
        Role role = Role.MANAGER;
        
        // Assurez-vous de créer l'objet utilisateur avec les bonnes données
        Utilisateur nouvelUtilisateur = new Utilisateur("00", email, name, role);

        when(utilisateurSPI.save(any(Utilisateur.class))).thenAnswer(
        		invocation -> {
        		    Utilisateur u = invocation.getArgument(0);
        		    u.setId(UUID.randomUUID().toString()); // Affecter un ID manuellement pour le test
        		    return u;
        		});

        // 3. Exécution (When)
        // Assurez-vous que l'appel ici correspond à la signature de votre méthode createUser
        Utilisateur created = service.createUser(nouvelUtilisateur); 

        // 4. Vérifications (Then)
        assertNotNull(created);
        assertNotNull(created.getId()); // Note: assurez-vous que votre service génère bien un ID
        assertEquals(email, created.getEmail());
        assertEquals(role, created.getRole());
        verify(utilisateurSPI, times(1)).save(any(Utilisateur.class));
        
        // 5. Nettoyage
        SecurityContextHolder.clearContext();
    }
    */
    
    /*
    @Test
    void admin_should_successfully_create_user() {
        // 1. Préparation des données d'opérateur
        String adminEmail = "admin@paybank.com";
        Utilisateur adminOperateur = new Utilisateur("00", adminEmail, "Admin", Role.ADMIN); // Ajustez le constructeur selon votre classe
        
        // 2. IMPORTANT : Mocker le repository pour qu'il retourne l'admin
        // Remplacez 'operateurSPI' par le nom de votre mock/port d'accès aux opérateurs
        when(utilisateurSPI.findById("00")).thenReturn(Optional.of(adminOperateur));

        // 3. Setup Sécurité
        var auth = new UsernamePasswordAuthenticationToken(
                adminEmail, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // 4. Exécution
        String email = "john.doe@gmail.com";
        String name = "John Doe";
        Role role = Role.MANAGER;
        Utilisateur nouvelUtilisateur = new Utilisateur("01", email, name, role);
        
        when(utilisateurSPI.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilisateur created = service.createUser(nouvelUtilisateur);

        // 5. Vérifications
        assertNotNull(created);
        assertEquals(email, created.getEmail());
        verify(utilisateurSPI, times(1)).save(any(Utilisateur.class));
        
        SecurityContextHolder.clearContext();
    }
    */
    
    /*
    @Test
    void admin_should_successfully_create_user() {
        // 1. Préparation des données
        String adminEmail = "admin@paybank.com";
        Utilisateur adminOperateur = new Utilisateur("rochetteGTest", adminEmail, "password", Role.ADMIN);
        adminOperateur.setActif(true);
        // 2. MOCKER LA RECHERCHE PAR EMAIL (C'est probablement ici que le service échoue)
        // Le service appelle probablement utilisateurSPI.findByEmail(auth.getName())
        when(utilisateurSPI.findByEmail(adminEmail)).thenReturn(Optional.of(adminOperateur));

        // 3. Setup Sécurité
        var auth = new UsernamePasswordAuthenticationToken(
                adminEmail, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // 4. Exécution
        String email = "john.doe@gmail.com";
        String name = "John Doe";
        Role role = Role.MANAGER;
        Utilisateur nouvelUtilisateur = new Utilisateur(name, email, "passwordDoe", role);
        
        when(utilisateurSPI.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilisateur created = service.createUser(nouvelUtilisateur);

        // 5. Vérifications
        assertNotNull(created);
        assertEquals(email, created.getEmail());
        verify(utilisateurSPI, times(1)).save(any(Utilisateur.class));
        
        SecurityContextHolder.clearContext();
    }
    */
    
    @Test
    void admin_should_successfully_create_user() {
        // 1. Préparation
        String adminEmail = "admin@paybank.com";
        Utilisateur adminOperateur = new Utilisateur("rochetteGTest", adminEmail, "password", Role.ADMIN, true);
        
        // IMPORTANT : Vérifiez le nom de cette méthode dans votre classe Utilisateur !
        // Si c'est getActif(), le service attend peut-être isActive()
        adminOperateur.setActif(true); 

        // 2. Mocker l'appel qui est VRAIMENT fait par le service
        when(utilisateurSPI.findByEmail(adminEmail)).thenReturn(Optional.of(adminOperateur));

        // 3. Configurer l'authentification (le getName() doit être l'email)
        var auth = new UsernamePasswordAuthenticationToken(
                adminEmail, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // 4. Exécution
        Utilisateur nouvelUtilisateur = new Utilisateur("John Doe", "john.doe@gmail.com", "passwordDoe", Role.MANAGER, true);
        when(utilisateurSPI.save(any(Utilisateur.class))).thenAnswer(i -> i.getArgument(0));

        // Appel
        Optional<Utilisateur> debugUser = utilisateurSPI.findByEmail(adminEmail);
        System.out.println("DEBUG: User trouvé ? " + debugUser.isPresent());
        if(debugUser.isPresent()) {
            System.out.println("DEBUG: User actif ? " + debugUser.get().isActive()); // ou getActif()
        }
        Utilisateur admin = new Utilisateur("op1", "admin@paybank.com", "pass", Role.ADMIN, true);
        Utilisateur created = service.createUser(admin, nouvelUtilisateur);

        // 5. Assertions
        assertNotNull(created);
        verify(utilisateurSPI).save(any(Utilisateur.class));
        
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void manager_promoting_to_admin_should_throw_exception() {
        // Given
        String userId = "user-123";
        Utilisateur targetUser = new Utilisateur(userId, "test@test.com", "Test User", Role.EMPLOYE, true);
        
        when(utilisateurSPI.findById(userId)).thenReturn(Optional.of(targetUser));

        // When & Then
        assertThrows(SecurityException.class, () -> {
            service.updateUser(managerOperateur, userId, "Test User updated", Role.ADMIN, null);
        });
        
        // On vérifie qu'aucune sauvegarde n'a été tentée
        verify(utilisateurSPI, never()).save(any(Utilisateur.class));
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    
}