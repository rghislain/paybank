package com.paybank.hexagonal.entite;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "passwords")
public class PasswordEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String role;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "updated_by")
    private String updatedBy; //Stocke l'identifiant VARCHAR de l'utilisateur

    // Constructeur par défaut requis par JPA
    public PasswordEntity() {
    }

    //Constructeur avec paramètres principaux
    public PasswordEntity(String id, String role, String passwordHash, String updatedBy) {
        this.id = id;
        this.role = role;
        this.passwordHash = passwordHash;
        this.updatedAt = OffsetDateTime.now();
        this.updatedBy = updatedBy;
    }

    //Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}