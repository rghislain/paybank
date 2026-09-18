package com.paybank.hexagonal.domaine.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Transaction(
    String id,
    long montantCentimes,
    LocalDate date,
    String reference,
    String type
) {}