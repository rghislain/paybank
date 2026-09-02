package com.paybank.hexagonal.domaine.controleur;

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
import com.paybank.hexagonal.domaine.Client;
import com.paybank.hexagonal.domaine.DashboardStats;
import com.paybank.hexagonal.domaine.service.DashboardService;
import com.paybank.hexagonal.domaine.service.MultiUtilisateursPaiementService;
import com.paybank.hexagonal.domaine.service.PaiementImplementationService;
import com.paybank.hexagonal.port.GetDashboardSPI;
import com.paybank.hexagonal.repository.AccountRepository;
import org.springframework.web.bind.annotation.RequestHeader;

/*
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final GetDashboardUseCase getDashboardUseCase;
    private final AccountRepository accountRepository;
    //private final DashboardService dashboardService;
    
    public DashboardController(GetDashboardUseCase getDashboardUseCase, AccountRepository accountRepository) {
        this.getDashboardUseCase = getDashboardUseCase;
		this.accountRepository = accountRepository;
		//this.dashboardService = null;
    }
    
    /*
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboard(@AuthenticationPrincipal Client principaly) {
    	@RequestHeader(value = "X-Auth-Role", defaultValue = "EMPLOYE") String role) {
            
            // Récupération dynamique d'un UUID valide présent dans votre base de données
            // (Prend le premier compte disponible, ou génère un UUID par défaut si la table est vide)
            UUID clientId = accountRepository.findAll()
                .stream()
                .map(acc -> acc.getId()) // Adaptez selon votre entité Account (ex: acc.getId() ou acc.getClientId())
                .findFirst()
                .orElseGet(UUID::randomUUID);

            // Si le rôle est ADMIN, vous pouvez aussi lui permettre d'accéder aux stats
            DashboardStats stats = getDashboardUseCase.getStatsForClient(clientId);
            return ResponseEntity.ok(stats);
    	}
    }
    */
   /* 
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboard(
            @RequestHeader(value = "X-Auth-Role", defaultValue = "EMPLOYE") String role) {
        
        // Récupération DYNAMIQUE du premier ID réel présent en BDD
        //UUID realClientId = accountRepository.findFirstAccountId()
           // .orElseThrow(() -> new RuntimeException("Aucun compte trouvé en base de données pour afficher le dashboard."));

        // Le service calcule les vraies statistiques de ce client depuis la BDD
        //DashboardStats stats = getDashboardUseCase.getStatsForClient(realClientId);
    	DashboardStats stats = getDashboardUseCase.getGlobalStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getGlobalDashboardStats() {
        // C'est cette méthode qui va chercher le solde total de la table accounts
        DashboardStats stats = getDashboardUseCase.getGlobalStats();
        
        return ResponseEntity.ok(stats);
    }
}
*/


@RestController
@RequestMapping("/api/dashboard")
public class DashboardControleur {

    private final DashboardService dashboardService;

    public DashboardControleur(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getGlobalDashboardStats() {
        // Appelle la méthode globale qui récupère la somme totale de la table accounts 
        // et les 5 dernières transactions de toute la banque
        DashboardStats stats = dashboardService.getGlobalStats();
        
        return ResponseEntity.ok(stats);
    }
}