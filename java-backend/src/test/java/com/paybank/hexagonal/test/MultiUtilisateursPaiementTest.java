package com.paybank.hexagonal.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.paybank.hexagonal.domaine.model.Role;
import com.paybank.hexagonal.domaine.model.Utilisateur;
import com.paybank.hexagonal.domaine.service.MultiUtilisateursPaiementService;
import com.paybank.hexagonal.main.PaiementApplication;
import com.paybank.hexagonal.sortie.port.ClientSPI;
import com.paybank.hexagonal.sortie.port.UtilisateurSPI;

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

class MultiUtilisateursPaiementTest {

    private UtilisateurSPI utilisateurSPI;
    private ClientSPI clientSPI;
    private MultiUtilisateursPaiementService service;
    private Utilisateur adminOperateur;
    private Utilisateur managerOperateur;

    @BeforeEach
    void setUp() {
        //Mock des interfaces (Ports de sortie)
        utilisateurSPI = Mockito.mock(UtilisateurSPI.class);
        clientSPI = Mockito.mock(ClientSPI.class);
        
        service = new MultiUtilisateursPaiementService(utilisateurSPI, clientSPI);

        //Création des profils d'opérateurs pour les tests
        adminOperateur = new Utilisateur("admin-1", "admin@paybank.com", "Admin Root", Role.ADMIN, true);
        managerOperateur = new Utilisateur("manager-1", "manager@paybank.com", "Manager Business", Role.MANAGER, true);
    }
  
    @Test
    void admin_should_successfully_create_user() {
        //1. Préparation
        String adminEmail = "admin@paybank.com";
        Utilisateur adminOperateur = new Utilisateur("rochetteGTest", adminEmail, "password", Role.ADMIN, true);
        
        adminOperateur.setActif(true); 

        //2. Mocker l'appel qui est VRAIMENT fait par le service
        when(utilisateurSPI.findByEmail(adminEmail)).thenReturn(Optional.of(adminOperateur));

        //3. Configurer l'authentification (le getName() doit être l'email)
        var auth = new UsernamePasswordAuthenticationToken(
                adminEmail, 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        //4. Exécution
        Utilisateur nouvelUtilisateur = new Utilisateur("John Doe", "john.doe@gmail.com", "passwordDoe", Role.MANAGER, true);
        when(utilisateurSPI.save(any(Utilisateur.class))).thenAnswer(i -> i.getArgument(0));

        //Appel
        Optional<Utilisateur> debugUser = utilisateurSPI.findByEmail(adminEmail);
        System.out.println("DEBUG: User trouvé ? " + debugUser.isPresent());
        if(debugUser.isPresent()) {
            System.out.println("DEBUG: User actif ? " + debugUser.get().isActive());
        }
        Utilisateur admin = new Utilisateur("op1", "admin@paybank.com", "pass", Role.ADMIN, true);
        Utilisateur created = service.createUser(admin, nouvelUtilisateur);

        //5. Assertions
        assertNotNull(created);
        verify(utilisateurSPI).save(any(Utilisateur.class));
        
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void manager_promoting_to_admin_should_throw_exception() {      
        String userId = "user-123";
        Utilisateur targetUser = new Utilisateur(userId, "test@test.com", "Test User", Role.EMPLOYE, true);
        
        when(utilisateurSPI.findById(userId)).thenReturn(Optional.of(targetUser));

        assertThrows(SecurityException.class, () -> {
            service.updateUser(managerOperateur, userId, "Test User updated", Role.ADMIN, null);
        });
        
        //On vérifie qu'aucune sauvegarde n'a été tentée
        verify(utilisateurSPI, never()).save(any(Utilisateur.class));
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    
}