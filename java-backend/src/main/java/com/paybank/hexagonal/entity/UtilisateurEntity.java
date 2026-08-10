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

    public UtilisateurEntity() {}

    // Convertisseur : Domaine -> Base de données
    public static UtilisateurEntity fromDomain(Utilisateur domaine) {
        if (domaine == null) return null;
        UtilisateurEntity entity = new UtilisateurEntity();
        entity.id = domaine.getId();
        entity.nom = domaine.getNom();
        entity.email = domaine.getEmail();
        entity.actif = domaine.isActive();
        entity.role = domaine.getRole();
        entity.password = domaine.getPassword();
        entity.actif = domaine.isActive();
        return entity;
    }

    // Convertisseur : Base de données -> Domaine (⚠️ FIXÉ : On passe 'this.role' au lieu de 'null')
    public Utilisateur toDomain() {
        Utilisateur user = new Utilisateur(this.nom, this.email, this.password, this.role, this.actif);
        if (!this.actif) {
            user.deactivate();
        }
        return user;
    }
    
    public boolean verifierMotDePasse(String passwordATester, BCryptPasswordEncoder encoder) {
        return encoder.matches(passwordATester, this.password);
    }

    // Getters & Setters standard (⚠️ FIXÉ : Le setter prend et applique 'role')
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

	public String getPassword() {
		return this.password;
	}
	
	public void setPassword(String password) {
	    this.password = password;
	}
}