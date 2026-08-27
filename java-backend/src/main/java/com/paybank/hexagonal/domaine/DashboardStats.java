package com.paybank.hexagonal.domaine;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStats(
	    BigDecimal currentBalance,			// KPI : Solde
	    long pendingTransactionsCount,		// KPI : Opérations en attente
	    BigDecimal monthlyRevenue,			// KPI : Revenus du mois
	    double activityTrendPercentage,      // KPI : Taux de croissance (ex: +12%)
	    List<Transaction> recentActivities	// Liste pour le tableau récapitulatif
	) {}