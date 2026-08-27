package com.paybank.hexagonal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts") // Nom de ta table en BDD
public class CompteEntity {
    @Id
    private UUID id;
    
    @Column(name = "balance")
    private BigDecimal balance;

    // Getters, Setters, et Constructeurs
    public BigDecimal getBalance() { return balance; }
}