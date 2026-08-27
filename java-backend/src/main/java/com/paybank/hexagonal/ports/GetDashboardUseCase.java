package com.paybank.hexagonal.ports;

import java.util.UUID;

import com.paybank.hexagonal.domaine.DashboardStats;

public interface GetDashboardUseCase {
    DashboardStats getStatsForClient(UUID clientId);
	DashboardStats getGlobalStats();
}