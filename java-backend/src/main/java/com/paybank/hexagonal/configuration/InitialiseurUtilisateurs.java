package com.paybank.hexagonal.configuration;

import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.UtilisateurRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class InitialiseurUtilisateurs {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${ADMIN_PASSWORD:adminPass1}")
    private String admin1Password;

    @Value("${MANAGER1_PASSWORD:managerPass1}")
    private String manager1Password;

    @Value("${EMPLOYE1_PASSWORD:employePass1}")
    private String employe1Password;

    @Bean
    CommandLineRunner initUsersOnce(UtilisateurRepository repository, BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            creerSiAbsent(repository, passwordEncoder, "ghislainrochette@paybank.com", "Ghislain Rochette", admin1Password, Role.ADMIN);
            creerSiAbsent(repository, passwordEncoder, "manager1@paybank.com", "Manager Un", manager1Password, Role.MANAGER);
            creerSiAbsent(repository, passwordEncoder, "employe1@paybank.com", "Employe Un", employe1Password, Role.EMPLOYE);
            
            System.out.println(">>> Vérification et initialisation des utilisateurs terminées.");
        };
    }

    private void creerSiAbsent(UtilisateurRepository repo, BCryptPasswordEncoder encoder, String email, String nom, String passwordClair, Role role) {
        if (repo.findByEmail(email).isEmpty()) {
            UtilisateurEntity nouveau = new UtilisateurEntity();
            nouveau.setId(UUID.randomUUID().toString());
            nouveau.setEmail(email);
            nouveau.setNom(nom);
            nouveau.setPassword(encoder.encode(passwordClair));
            nouveau.setRole(role);
            nouveau.setActif(true);
            
            repo.save(nouveau);
            System.out.println(">>> Utilisateur créé avec succès : " + email);
        }
    }
}