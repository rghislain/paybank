package com.paybank.hexagonal.ports;

import com.paybank.hexagonal.domaine.FinancialReport;
import java.time.LocalDate;
import java.util.UUID;

public interface FinancialReportUseCase {
    FinancialReport generateReport(UUID clientId, LocalDate start, LocalDate end);
    void issueInvoice(UUID clientId, UUID transactionId); // Émission de facture
}