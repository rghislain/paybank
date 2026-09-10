package com.paybank.hexagonal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    private UUID id;
    private UUID clientId;
    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private boolean isDebit;
    private String reconciliationStatus;
    private String status; //Ex: "PENDING", "COMPLETED"

    //Getters
    public UUID getId() { return id; }
    public UUID getClientId() { return clientId; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public boolean isDebit() { return isDebit; }
    public String getReconciliationStatus() { return reconciliationStatus; }
    public String getStatus() { return status; }

    //Setters (très utiles pour le DataLoader ou la création)
    public void setId(UUID id) { this.id = id; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDebit(boolean debit) { isDebit = debit; }
    public void setReconciliationStatus(String reconciliationStatus) { this.reconciliationStatus = reconciliationStatus; }
    public void setStatus(String status) { this.status = status; }
}