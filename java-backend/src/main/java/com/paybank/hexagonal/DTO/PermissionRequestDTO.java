package com.paybank.hexagonal.DTO;

public class PermissionRequestDTO {
    private String ressource;
    private String roleCible;
    private String action;
    private boolean granted;

    // --- Getters et Setters ---
    public String getRessource() {
        return ressource;
    }

    public void setRessource(String ressource) {
        this.ressource = ressource;
    }

    public String getRoleCible() {
        return roleCible;
    }

    public void setRoleCible(String roleCible) {
        this.roleCible = roleCible;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }
}