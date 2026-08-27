package com.paybank.hexagonal.DTO;

import com.paybank.hexagonal.domaine.Role;

public class DroitRequestDTO {
	private String ressource;
	private Role role;
    private String action;
    //private boolean valeur;
    private boolean granted;

    // Getters et Setters
    public String getRessource() { return ressource; }
    public void setRessource(String ressource) { this.ressource = ressource; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    //public boolean isValeur() { return valeur; }
    //public void setValeur(boolean valeur) { this.valeur = valeur; }

    public boolean isGranted() { return granted; }
    public void setGranted(boolean granted) { this.granted = granted; }

}