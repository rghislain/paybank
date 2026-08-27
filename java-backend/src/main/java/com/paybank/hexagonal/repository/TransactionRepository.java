package com.paybank.hexagonal.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import com.paybank.hexagonal.domaine.RecentActivity;
import com.paybank.hexagonal.domaine.Transaction;
import com.paybank.hexagonal.domaine.TransactionDetail;

public interface TransactionRepository {
    long countPending(UUID clientId);
 // Nouvelle méthode pour le calcul des revenus
    BigDecimal getMonthlyRevenue(UUID clientId, YearMonth month);
    
    // Nouvelle méthode pour l'historique
    List<Transaction> findRecentActivities(UUID clientId, int limit);
    List<TransactionDetail> findTransactionsByDateRange(UUID clientId, LocalDate startDate, LocalDate endDate);
	long countAllPending();
	BigDecimal calculateGlobalMonthlyRevenue();
	List<Transaction> findTop5RecentActivities();
}
