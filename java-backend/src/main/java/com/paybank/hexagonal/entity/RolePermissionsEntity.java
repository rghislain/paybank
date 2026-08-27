package com.paybank.hexagonal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role_permissions")
public class RolePermissionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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