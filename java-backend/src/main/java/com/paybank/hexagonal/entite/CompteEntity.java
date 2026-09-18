package com.paybank.hexagonal.entite;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts") //nom de la table en BDD
public class CompteEntity {
    @Id
    private UUID id;
    
    @Column(name = "balance")
    private BigDecimal balance;

    public BigDecimal getBalance() { return balance; }
}