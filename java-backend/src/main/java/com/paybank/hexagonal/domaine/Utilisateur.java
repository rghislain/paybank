package com.paybank.hexagonal.domaine;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Utilisateur {
    
    private String id;
    private String email;
    private String nom;
    private Role role;
    private boolean actif=true;
    private String password;
    private boolean lire;
    private boolean creer;
    private boolean modifier;
    private boolean supprimer;
    private boolean sauvegarder;
    private boolean imprimer;

    public Utilisateur(String nom, String email, String password, Role role, boolean actif) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.nom = nom;
        this.role = role;
        this.password = password;
        this.actif = true;
    }
    
    public Utilisateur() {}
    
    public boolean verifierMotDePasse(String passwordATester, BCryptPasswordEncoder encoder) {
        return encoder.matches(passwordATester, this.password);
    }
    
    public boolean aLeDroit(String action) {
        if (this.role == Role.ADMIN) {
            return true; // L'Admin a tous les droits par défaut, ou adaptez selon vos règles
        }
        switch (action.toUpperCase()) {
            case "LIRE":
                return this.lire;
            case "CREER":
                return this.creer;
            case "MODIFIER":
                return this.modifier;
            case "SUPPRIMER":
                return this.supprimer;
            case "SAUVEGARDER":
                return this.sauvegarder;
            case "IMPRIMER":
                return this.imprimer;
            default:
                return false;
        }
    }
    
    // Getters & Setters purement Java
    public String getId() { return id; }
    public String getEmail() { return this.email; }
    public String getNom() { return this.nom; }
    public Role getRole() { return role; }
    public boolean isActive() { return actif; }
    public String getPassword() {
        return this.password;
    }   
    public void setRole(Role role) { this.role = role; }
    public void setName(String name) { this.nom = name; }
    public void deactivate() { this.actif = false; }
	public void setEmail(String string) {
		this.email=string;
	}
	public void setId(String uuid) {
		this.id=uuid;
	}
	public void setActif(boolean b) {
		this.actif=b;
	}	
	public void setPassword(String password) {
        this.password=password;
    }
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