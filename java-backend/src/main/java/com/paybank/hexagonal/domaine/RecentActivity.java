package com.paybank.hexagonal.domaine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecentActivity(
    UUID id,
    String description,
    BigDecimal amount,
    LocalDateTime date,
    String type // Exemple : "CREDIT" ou "DEBIT"
) {}