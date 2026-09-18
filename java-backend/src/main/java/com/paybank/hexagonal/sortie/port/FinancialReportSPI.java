package com.paybank.hexagonal.sortie.port;

import java.time.LocalDate;
import java.util.UUID;

import com.paybank.hexagonal.domaine.model.FinancialReport;

public interface FinancialReportSPI {
    FinancialReport generateReport(UUID clientId, LocalDate start, LocalDate end);
    void issueInvoice(UUID clientId, UUID transactionId); // Émission de facture
}