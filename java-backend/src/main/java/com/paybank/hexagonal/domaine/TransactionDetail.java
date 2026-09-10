package com.paybank.hexagonal.domaine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionDetail(
    UUID id,
    LocalDate date,
    String description,
    BigDecimal amount,
    boolean isDebit, //True si c'est un débit, False si c'est un crédit
    String reconciliationStatus //Ex: "RAPPROCHE", "EN_ATTENTE" (pour le rapprochement bancaire)
) {}