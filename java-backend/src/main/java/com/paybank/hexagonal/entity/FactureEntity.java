package com.paybank.hexagonal.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "facture_entity")
public class FactureEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
    private UUID id;
	
    private UUID client_id;
    private LocalDate date_echeance;
    private Instant date_emission;
    private BigDecimal montant_total;
    private String statut;
    private String client_email;
    private String euros;
    private String nom;
    
    @Column(name = "paiements_id")
    private String paiementsId;
   
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getClientId() { return client_id; }
    public void setClientId(UUID clientId) { this.client_id = clientId; }
    
    public Instant getDateEmission() { return date_emission; }
    public void setDateEmission(Instant instant) { this.date_emission = instant; }
    
    public LocalDate getDateEcheance() { return date_echeance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.date_echeance = dateEcheance; }
    
    public BigDecimal getMontantTotal() { return montant_total; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montant_total = montantTotal; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
	
    public String getPaiementId() { return this.paiementsId; }
    public void setPaiementId(String paiementId) {
		this.paiementsId=paiementId;		
	}
	public void setClientNom(String nom) {
		this.nom=nom;
	}
	public void setClientEmail(String email) {
		this.client_email=email;
	}
	public void setDevise(String devise) {
		this.euros=devise;		
	}
		
}