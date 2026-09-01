package com.paybank.hexagonal.DTO;

public class AuthResponseDto {
	private String id;
    private String token;
    private String email;
    private String nom;
    private String role;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, String email, String nom, String role) {
        this.token = token;
        this.email = email;
        this.nom = nom;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}