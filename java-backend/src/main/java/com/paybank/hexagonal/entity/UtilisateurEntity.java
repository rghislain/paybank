package com.paybank.hexagonal.entity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.Utilisateur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "utilisateurs")
public class UtilisateurEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String nom;
    private String email;
    private String password;
  
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
    
    @Column(name = "actif")
    private boolean actif;
    
    // Permissions directement dans la table utilisateurs
    @Column(nullable = false)
    private boolean lire;
    
    @Column(nullable = false)
    private boolean creer;
    
    @Column(nullable = false)
    private boolean modifier;
    
    @Column(nullable = false)
    private boolean supprimer;
    
    @Column(nullable = false)
    private boolean sauvegarder;
    
    @Column(nullable = false)
    private boolean imprimer;

    public UtilisateurEntity() {}

    // Convertisseur : Domaine -> Base de données
    public static UtilisateurEntity fromDomain(Utilisateur domaine) {
        if (domaine == null) return null;
        UtilisateurEntity entity = new UtilisateurEntity();
        entity.id = domaine.getId();
        entity.nom = domaine.getNom();
        entity.email = domaine.getEmail();
        entity.role = domaine.getRole();
        entity.password = domaine.getPassword();
        entity.actif = domaine.isActive(); // Doublon nettoyé
        
        // Mapping direct des droits
        entity.lire = domaine.isLire();
        entity.creer = domaine.isCreer();
        entity.modifier = domaine.isModifier();
        entity.supprimer = domaine.isSupprimer();
        entity.sauvegarder = domaine.isSauvegarder();
        entity.imprimer = domaine.isImprimer();
        
        return entity;
    }

    // Convertisseur : Base de données -> Domaine
    public Utilisateur toDomain() {
        Utilisateur user = new Utilisateur(this.nom, this.email, this.password, this.role, this.actif);
        user.setId(this.id);
        if (!this.actif) {
            user.deactivate();
        }
        
        // Mapping direct des droits vers le domaine
        user.setLire(this.lire);
        user.setCreer(this.creer);
        user.setModifier(this.modifier);
        user.setSupprimer(this.supprimer);
        user.setSauvegarder(this.sauvegarder);
        user.setImprimer(this.imprimer);
        
        return user;
    }
    
    public boolean verifierMotDePasse(String passwordATester, BCryptPasswordEncoder encoder) {
        return encoder.matches(passwordATester, this.password);
    }

    // Getters & Setters standard
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
    
    public boolean isLire() { return lire; }
    public void setLire(boolean lire) { this.lire = lire; }
    
    public boolean isCreer() { return creer; }
    public void setCreer(boolean creer) { this.creer = creer; }
    
    public boolean isModifier() { return modifier; }
    public void setModifier(boolean modifier) { this.modifier = modifier; }
    
    public boolean isSupprimer() { return supprimer; }
    public void setSupprimer(boolean supprimer) { this.supprimer = supprimer; }
    
    public boolean isSauvegarder() { return sauvegarder; }
    public void setSauvegarder(boolean sauvegarder) { this.sauvegarder = sauvegarder; }
    
    public boolean isImprimer() { return imprimer; }
    public void setImprimer(boolean imprimer) { this.imprimer = imprimer; }
}