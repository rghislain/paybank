package com.paybank.hexagonal.port;

import java.util.UUID;
import com.paybank.hexagonal.domaine.DashboardStats;

public interface GetDashboardSPI {
    DashboardStats getStatsForClient(UUID clientId);
	DashboardStats getGlobalStats();
}