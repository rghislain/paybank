package com.paybank.hexagonal.entite;

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

    //--- Getters et Setters ---
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
}