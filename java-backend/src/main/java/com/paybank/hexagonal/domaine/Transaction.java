package com.paybank.hexagonal.domaine;

import java.time.LocalDate;

public record Transaction(
    String id,
    long montantCentimes,
    LocalDate date,
    String reference,
    String type
) {}