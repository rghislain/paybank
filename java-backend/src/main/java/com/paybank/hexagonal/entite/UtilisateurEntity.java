package com.paybank.hexagonal.entite;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paybank.hexagonal.domaine.model.Role;
import com.paybank.hexagonal.domaine.model.Utilisateur;

@Entity
@Table(name = "utilisateurs")
public class UtilisateurEntity {

    @Id
    private String id=UUID.randomUUID().toString();;

    private String nom;
    private String email;
    private String password;
    private boolean actif;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    //--- Méthodes de mapping Domain <-> Entity ---   
    public Utilisateur toDomain() {
        Utilisateur domaine = new Utilisateur();
        domaine.setId(this.id);
        domaine.setNom(this.nom);
        domaine.setEmail(this.email);
        domaine.setPassword(this.password);
        domaine.setActif(this.actif);
        domaine.setRole(this.role);       
        return domaine;
    }
        
    public static UtilisateurEntity fromDomain(Utilisateur domaine) {
        if (domaine == null) {
            return null;
        }
        UtilisateurEntity entity = new UtilisateurEntity();
        entity.setId(domaine.getId());
        entity.setNom(domaine.getNom());
        entity.setEmail(domaine.getEmail());
        entity.setPassword(domaine.getPassword());
        entity.setActif(domaine.getActif());
        entity.setRole(domaine.getRole());
        return entity;
    }
    
    //--- Getters et Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean getActif() { return actif; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}