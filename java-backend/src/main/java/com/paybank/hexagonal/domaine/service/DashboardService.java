package com.paybank.hexagonal.domaine.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.paybank.hexagonal.domaine.model.DashboardStats;
import com.paybank.hexagonal.domaine.model.Transaction;
import com.paybank.hexagonal.jpaRepository.AccountRepository;
import com.paybank.hexagonal.jpaRepository.TransactionRepository;
import com.paybank.hexagonal.sortie.port.GetDashboardSPI;

@Service
public class DashboardService implements GetDashboardSPI {
    private final AccountRepository accountRepo; //Port de sortie
    private final TransactionRepository transRepo; //Port de sortie

    //Injection des dépendances via le constructeur
    public DashboardService(AccountRepository accountRepo, TransactionRepository transRepo) {
        this.accountRepo = accountRepo;
        this.transRepo = transRepo;
    }

    @Override
    public DashboardStats getStatsForClient(UUID clientId) {
        //1. Données brutes
        BigDecimal balance = accountRepo.findBalanceByClient(clientId);
        long pendingCount = transRepo.countPending(clientId);
        
        //2. Calcul des revenus (Mois en cours vs Mois précédent pour la tendance)
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        
        BigDecimal currentRevenue = transRepo.getMonthlyRevenue(clientId, currentMonth);
        BigDecimal previousRevenue = transRepo.getMonthlyRevenue(clientId, previousMonth);
        
        //3. Calcul de la tendance (KPI)
        double trend = calculateTrend(currentRevenue, previousRevenue);
        
        //4. Activités récentes
        List<Transaction> activities = transRepo.findRecentActivities(clientId, 10);

        return new DashboardStats(balance, pendingCount, currentRevenue, trend, activities);
    }
    
    public DashboardStats getGlobalStats() {
        //Somme de tous les soldes de tous les comptes de la banque
        BigDecimal totalBalance = accountRepo.getTotalBalance();
        
        //nombre total d'opérations en attente
        long pendingOperations = transRepo.countAllPending();
              
        //Revenus globaux du mois
        BigDecimal monthlyRevenue = transRepo.calculateGlobalMonthlyRevenue();
        
        //Calcul de la tendance
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);       
        BigDecimal currentRevenue = transRepo.calculateMonthlyRevenueDate(currentMonth);
        BigDecimal previousRevenue = transRepo.calculateMonthlyRevenueDate(previousMonth);     
        double trend = calculateTrend(currentRevenue, previousRevenue);
        System.out.println("DEBUG - Current: " + currentRevenue + " | Previous: " + previousRevenue);
        //5 dernières activités récentes de toute la banque
        List<Transaction> recentActivities = transRepo.findTop5RecentActivities();
        
        return new DashboardStats(totalBalance, pendingOperations, monthlyRevenue, trend, recentActivities);
    }
    
    private double calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
        	return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
        		.multiply(new BigDecimal("100"))      
        		.divide(previous, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}