package com.paybank.hexagonal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role_cible", "ressource", "action"})
})
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_cible", nullable = false)
    private String roleCible; // Correspond au champ 'role' de la table utilisateurs

    @Column(nullable = false)
    private String ressource; 

    @Column(nullable = false)
    private String action; 

    @Column(name = "is_granted", nullable = false)
    private boolean isGranted;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleCible() { return roleCible; }
    public void setRoleCible(String roleCible) { this.roleCible = roleCible; }
    public String getRessource() { return ressource; }
    public void setRessource(String ressource) { this.ressource = ressource; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public boolean isGranted() { return isGranted; }
    public void setGranted(boolean granted) { isGranted = granted; }
}