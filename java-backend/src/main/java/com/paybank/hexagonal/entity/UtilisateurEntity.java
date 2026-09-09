package com.paybank.hexagonal.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.Utilisateur;

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

    /*
    private boolean creer;
    private boolean modifier;
    private boolean supprimer;
    private boolean lire;
    private boolean sauvegarder;
    private boolean imprimer;
    */

    //@ToString.Exclude
    //@EqualsAndHashCode.Exclude
    //@OneToOne(mappedBy = "utilisateur", fetch = FetchType.LAZY)
    //@OneToMany(fetch = FetchType.LAZY)
    //private RessourcesEntity ressource;
    
    //@OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<RessourcesEntity> ressources = new ArrayList<>();

    // --- Méthodes de mapping Domain <-> Entity ---

    
    public Utilisateur toDomain() {
        Utilisateur domaine = new Utilisateur();
        domaine.setId(this.id);
        domaine.setNom(this.nom);
        domaine.setEmail(this.email);
        domaine.setPassword(this.password);
        domaine.setActif(this.actif);
        domaine.setRole(this.role);
        /*
        domaine.setCreer(this.creer);
        domaine.setModifier(this.modifier);
        domaine.setSupprimer(this.supprimer);
        domaine.setLire(this.lire);
        domaine.setSauvegarder(this.sauvegarder);
        domaine.setImprimer(this.imprimer);
        */
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
        /*
        entity.setCreer(domaine.isCreer());
        entity.setModifier(domaine.isModifier());
        entity.setSupprimer(domaine.isSupprimer());
        entity.setLire(domaine.isLire());
        entity.setSauvegarder(domaine.isSauvegarder());
        entity.setImprimer(domaine.isImprimer());
        */
        return entity;
    }
    

    // --- Getters et Setters ---

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

    /*
    public boolean isCreer() { return creer; }
    public void setCreer(boolean creer) { this.creer = creer; }

    public boolean isModifier() { return modifier; }
    public void setModifier(boolean modifier) { this.modifier = modifier; }

    public boolean isSupprimer() { return supprimer; }
    public void setSupprimer(boolean supprimer) { this.supprimer = supprimer; }

    public boolean isLire() { return lire; }
    public void setLire(boolean lire) { this.lire = lire; }

    public boolean isSauvegarder() { return sauvegarder; }
    public void setSauvegarder(boolean sauvegarder) { this.sauvegarder = sauvegarder; }

    public boolean isImprimer() { return imprimer; }
    public void setImprimer(boolean imprimer) { this.imprimer = imprimer; }

    public List<RessourcesEntity> getRessources() { return ressources; }
    public void setRessources(List<RessourcesEntity> ressources) { this.ressources = ressources; }
	*/
}