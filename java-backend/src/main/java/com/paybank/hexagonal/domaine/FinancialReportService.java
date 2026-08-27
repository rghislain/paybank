package com.paybank.hexagonal.domaine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.paybank.hexagonal.ports.FinancialReportUseCase;
import com.paybank.hexagonal.repository.TransactionRepository;

@Service
public class FinancialReportService implements FinancialReportUseCase {
    private final TransactionRepository transRepo;

    public FinancialReportService(TransactionRepository transRepo) {
        this.transRepo = transRepo;
    }

    @Override
    public FinancialReport generateReport(UUID clientId, LocalDate start, LocalDate end) {
        // 1. Récupération des transactions sur la période
        List<TransactionDetail> transactions = transRepo.findTransactionsByDateRange(clientId, start, end);
        
        // 2. Calcul du total des débits (sorties d'argent)
        BigDecimal debits = transactions.stream()
            .filter(TransactionDetail::isDebit)
            .map(TransactionDetail::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // 3. Calcul du total des crédits (entrées d'argent)
        BigDecimal credits = transactions.stream()
            .filter(t -> !t.isDebit())
            .map(TransactionDetail::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // 4. Calcul du solde final pour cette période (Crédits - Débits)
        BigDecimal finalBalance = credits.subtract(debits);
        
        // 5. Retour du rapport complet
        return new FinancialReport(start, end, debits, credits, finalBalance, transactions);
    }
    
    @Override
    public void issueInvoice(UUID clientId, UUID transactionId) {
        // Logique métier pour générer ou lier une facture à cette transaction
        // (Exemple : enregistrer la demande en base de données ou générer un PDF)
    }
}