package com.paybank.hexagonal.DTO;

public class DroitUpdateRequestDTO {
    private String role;
    private String ressource;
    private String action;
    private boolean granted;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getRessource() { return ressource; }
    public void setRessource(String ressource) { this.ressource = ressource; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public boolean isGranted() { return granted; }
    public void setGranted(boolean granted) { this.granted = granted; }
}