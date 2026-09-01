package com.paybank.hexagonal.DTO;

public class LoginRequestDTO {
    
    private String username;
    private String password;
    private String email; // ou username selon votre base
   

    // Getters et Setters obligatoires pour que Spring puisse mapper le JSON
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}