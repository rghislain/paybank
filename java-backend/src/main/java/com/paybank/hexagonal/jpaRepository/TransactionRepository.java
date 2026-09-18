package com.paybank.hexagonal.jpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.paybank.hexagonal.domaine.model.RecentActivity;
import com.paybank.hexagonal.domaine.model.Transaction;
import com.paybank.hexagonal.domaine.model.TransactionDetail;

@Repository
public interface TransactionRepository {
    long countPending(UUID clientId);
    //nouvelle méthode pour le calcul des revenus
    BigDecimal getMonthlyRevenue(UUID clientId, YearMonth month);
    //méthode pour l'historique
    List<Transaction> findRecentActivities(UUID clientId, int limit);
    List<TransactionDetail> findTransactionsByDateRange(UUID clientId, LocalDate startDate, LocalDate endDate);
	long countAllPending();
	BigDecimal calculateGlobalMonthlyRevenue();
	List<Transaction> findTop5RecentActivities();
	BigDecimal calculateMonthlyRevenueDate(YearMonth month);
}
