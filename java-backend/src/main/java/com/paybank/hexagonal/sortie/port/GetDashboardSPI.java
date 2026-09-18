package com.paybank.hexagonal.sortie.port;

import java.util.UUID;

import com.paybank.hexagonal.domaine.model.DashboardStats;

public interface GetDashboardSPI {
    DashboardStats getStatsForClient(UUID clientId);
	DashboardStats getGlobalStats();
}