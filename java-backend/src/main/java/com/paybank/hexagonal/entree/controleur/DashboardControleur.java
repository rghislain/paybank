package com.paybank.hexagonal.entree.controleur;

import java.math.BigDecimal;
import java.util.UUID;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paybank.hexagonal.domaine.model.Client;
import com.paybank.hexagonal.domaine.model.DashboardStats;
import com.paybank.hexagonal.domaine.service.DashboardService;
import com.paybank.hexagonal.domaine.service.MultiUtilisateursPaiementService;
import com.paybank.hexagonal.jpaRepository.AccountRepository;
import com.paybank.hexagonal.sortie.port.GetDashboardSPI;

import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardControleur {
    private final DashboardService dashboardService;

    public DashboardControleur(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getGlobalDashboardStats() {
        //Appelle la méthode globale qui récupère la somme totale de la table accounts 
        //et les 5 dernières transactions de toute la banque
        DashboardStats stats = dashboardService.getGlobalStats();       
        return ResponseEntity.ok(stats);
    }
}