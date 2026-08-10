package com.paybank.hexagonal.adaptateurs;

import com.paybank.hexagonal.DTO.DemandePaiementDto;
import com.paybank.hexagonal.domaine.TransactionPaiement;
import com.paybank.hexagonal.ports.ExecutionPaiementUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class CheckAdaptateur {

	/*
    private final ExecutionPaiementUseCase executionPaiementUseCase;

    public CheckAdaptateur(ExecutionPaiementUseCase executionPaiementUseCase) {
        this.executionPaiementUseCase = executionPaiementUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionPaiement> initierPaiement(
            @RequestAttribute("authenticatedUserId") UUID userIdFromJwt, // Injecté de manière sécurisée par l'AOP
            @RequestBody DemandePaiementDto dto) {

        TransactionPaiement resultat = executionPaiementUseCase.traiterPaiement(
                userIdFromJwt,
                dto.getMontantCentimes(),
                dto.getCleIdempotence(),
                dto.getTokenCarteMokbank()
        );

        return ResponseEntity.ok(resultat);
    }
    */
	
	@PostMapping(consumes = org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> simulerPaiement(@RequestParam Map<String, String> params) {
        
        // Simule la réponse attendue par ton adaptateur Stripe
        Map<String, Object> response = new HashMap<>();
        response.put("paid", true);
        response.put("status", "succeeded");
        
        return ResponseEntity.ok(response);
    }
	
}