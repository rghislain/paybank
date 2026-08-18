package com.paybank.hexagonal.domaine;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Utilisateur {
    
    private String id;
    private String email;
    private String nom;
    private Role role;
    private boolean actif;
    private String password;

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

}