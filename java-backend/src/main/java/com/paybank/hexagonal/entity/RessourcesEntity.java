package com.paybank.hexagonal.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ressources")
//@Getter @Setter // <--- Indispensable pour que les setX() modifient les champs
public class RessourcesEntity {

    @Id
    private UUID id;

    @Column(name = "utilisateurs_id", nullable = false)
    private String utilisateursId;

    private boolean clients;
    private boolean paiements;
    private boolean produits;
    
    @Column(name = "rapports_financiers")
    private boolean rapportsFinanciers;
    
    @Column(name = "parametres_systemes")
    private boolean parametresSystemes;

    
    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUtilisateursId() { return utilisateursId; }
    public void setUtilisateursId(String utilisateursId) { this.utilisateursId = utilisateursId; }

    public boolean getClients() { return clients; }
    public void setClients(boolean clients) { this.clients = clients; }

    public boolean getPaiements() { return paiements; }
    public void setPaiements(boolean paiements) { this.paiements = paiements; }
	
    public boolean getProduits() {
        return produits;
    }

    public void setProduits(boolean produits) {
        this.produits = produits;
    }

    public boolean getRapportsFinanciers() {
        return rapportsFinanciers;
    }

    public void setRapportsFinanciers(boolean rapportsFinanciers) {
        this.rapportsFinanciers = rapportsFinanciers;
    }

    public boolean getParametresSystemes() {
        return parametresSystemes;
    }

    public void setParametresSystemes(boolean parametresSystemes) {
        this.parametresSystemes = parametresSystemes;
    }
       
}