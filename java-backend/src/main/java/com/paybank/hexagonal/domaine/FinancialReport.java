package com.paybank.hexagonal.domaine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FinancialReport(
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalDebits,
    BigDecimal totalCredits,
    BigDecimal finalBalance,
    List<TransactionDetail> transactions //Liste détaillée avec rapprochement
) {}