package com.paybank.hexagonal.domaine;

import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Utilisateur {
    
    private String id;
    private String email;
    private String name;
    private Role role;
    private boolean active;
    private String password;

    public Utilisateur(String nom, String email, String password, Role role, boolean actif) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.name = nom;
        this.role = role;
        this.password = password;
        this.active = true;
    }
    
    public Utilisateur() {}
    
    public boolean verifierMotDePasse(String passwordATester, BCryptPasswordEncoder encoder) {
        return encoder.matches(passwordATester, this.password);
    }
    
    // Getters & Setters purement Java
    public String getId() { return id; }
    public String getEmail() { return this.email; }
    public String getNom() { return this.name; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public String getPassword() {
        return this.password;
    }
    
    public void setRole(Role role) { this.role = role; }
    public void setName(String name) { this.name = name; }
    public void deactivate() { this.active = false; }

	public void setEmail(String string) {
		this.email=string;
	}

	public void setId(String uuid) {
		this.id=uuid;
	}

	public void setActif(boolean b) {
		this.active=b;
	}
	
	public void setPassword(String password) {
        this.password=password;
    }

}