package com.paybank.hexagonal.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "role_permissions")
public class RolePermissionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "ressource", nullable = false, length = 50)
    private String ressource;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "granted", nullable = false)
    private boolean granted;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public RolePermissionsEntity() {}

    // --- Getters et Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRessource() { return ressource; }
    public void setRessource(String ressource) { this.ressource = ressource; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public boolean isGranted() { return granted; }
    public void setGranted(boolean granted) { this.granted = granted; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    /*
    @Column(name = "lire")
    private boolean lire;

    @Column(name = "creer")
    private boolean creer;

    @Column(name = "modifier")
    private boolean modifier;

    @Column(name = "supprimer")
    private boolean supprimer;

    @Column(name = "sauvegarder")
    private boolean sauvegarder;

    @Column(name = "imprimer")
    private boolean imprimer;

    public RolePermissionsEntity() {}

    // --- Getters et Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
	*/

}