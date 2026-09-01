package com.paybank.hexagonal.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

@Entity
@Table(name = "ressources")
public class RessourcesEntity {

    @Id
    private UUID id;

    private boolean clients;
    private boolean paiements;
    private boolean produits;
    
    @Column(name = "rapports_financiers")
    private boolean rapportsFinanciers;
    
    @Column(name = "parametres_systemes")
    private boolean parametresSystemes;
    
    
    private boolean utilisateurs;
    
    //@ToString.Exclude
    //@EqualsAndHashCode.Exclude
    //@OneToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "utilisateurs_id")
    //private UtilisateurEntity utilisateur;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateurs_id")
    private UtilisateurEntity utilisateur;

    // --- Getters et Setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    
    public boolean getClients() { return clients; }
    public void setClients(boolean clients) { this.clients = clients; }

    public boolean getPaiements() { return paiements; }
    public void setPaiements(boolean paiements) { this.paiements = paiements; }

    public boolean getProduits() { return produits; }
    public void setProduits(boolean produits) { this.produits = produits; }

    public boolean getRapportsFinanciers() { return rapportsFinanciers; }
    public void setRapportsFinanciers(boolean rapportsFinanciers) { this.rapportsFinanciers = rapportsFinanciers; }

    public boolean getParametresSystemes() { return parametresSystemes; }
    public void setParametresSystemes(boolean parametresSystemes) { this.parametresSystemes = parametresSystemes; }
     
    public boolean getUtilisateurs() { return utilisateurs; }
    public void setUtilisateurs(boolean utilisateurs) { this.utilisateurs = utilisateurs; }

    public UtilisateurEntity getUtilisateur() { return utilisateur; }
    public void setUtilisateur(UtilisateurEntity utilisateur) { this.utilisateur = utilisateur; }
}