package com.paybank.hexagonal.entree.controleur;

import com.paybank.hexagonal.DTO.DemandePaiementDTO;
import com.paybank.hexagonal.domaine.model.TransactionPaiement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class CheckControleur {

	@PostMapping(consumes = org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> simulerPaiement(@RequestParam Map<String, String> params) {        
        //Simule la réponse attendue par l'adaptateur Stripe
        Map<String, Object> response = new HashMap<>();
        response.put("paid", true);
        response.put("status", "succeeded");       
        return ResponseEntity.ok(response);
    }
	
}