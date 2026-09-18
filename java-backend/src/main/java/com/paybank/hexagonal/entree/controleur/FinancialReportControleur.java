package com.paybank.hexagonal.entree.controleur;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paybank.hexagonal.annotation.SecuredPermission;
import com.paybank.hexagonal.domaine.model.CustomUserDetails;
import com.paybank.hexagonal.domaine.model.FinancialReport;
import com.paybank.hexagonal.sortie.port.FinancialReportSPI;

@RestController
@RequestMapping("/api/client/bilan")
public class FinancialReportControleur {

    private final FinancialReportSPI reportUseCase;

    public FinancialReportControleur(FinancialReportSPI reportUseCase) {
        this.reportUseCase = reportUseCase;
    }

    // Récupérer le bilan filtré
    @GetMapping
    public ResponseEntity<FinancialReport> getBilan(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return ResponseEntity.ok(reportUseCase.generateReport(user.getUserId(), start, end));
    }

    // Émettre une facture depuis le bilan
    @PostMapping("/{transactionId}/invoice")
    public ResponseEntity<Void> generateInvoice(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID transactionId) {
        reportUseCase.issueInvoice(user.getUserId(), transactionId);
        return ResponseEntity.accepted().build();
    }
    
    @PostMapping("/clients/{clientId}/transactions/{transactionId}/invoice")
    public ResponseEntity<String> emitInvoice(
            @PathVariable UUID clientId, 
            @PathVariable UUID transactionId) {
        
        // Appel de la méthode métier
        reportUseCase.issueInvoice(clientId, transactionId);
        
        return ResponseEntity.ok("Facture générée, envoyée par e-mail aux destinataires et transmise à l'impression !");
    }
    
}