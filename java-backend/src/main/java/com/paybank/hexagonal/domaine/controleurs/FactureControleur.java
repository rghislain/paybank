package com.paybank.hexagonal.domaine.controleurs;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.paybank.hexagonal.domaine.ServiceFacture;
import com.paybank.hexagonal.domaine.annotation.RequireDroit;

@RestController
@RequestMapping("/api/factures")
public class FactureControleur {

    private final ServiceFacture serviceFacture;

    public FactureControleur(ServiceFacture serviceFacture) {
        this.serviceFacture = serviceFacture;
    }

    // --- 1. Générer facture (PDF affiché dans un nouvel onglet) ---
    @RequireDroit(action = "creer", ressource = "paiements")
    @GetMapping("/{paiementId}/generer")
    public ResponseEntity<byte[]> genererFacture(@PathVariable String paiementId, @RequestParam UUID clientId) {
        try {
            byte[] pdf = serviceFacture.genererFacturePdf(paiementId, clientId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=facture-" + paiementId + ".pdf")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                ("Erreur lors de la génération de la facture : " + e.getMessage()).getBytes(StandardCharsets.UTF_8)
            );
        }
    }

    // --- 2. Imprimer facture (même PDF, gate différente ; l'auto-impression est déclenchée côté front) ---
    @RequireDroit(action = "imprimer", ressource = "paiements")
    @GetMapping("/{paiementId}/imprimer")
    public ResponseEntity<byte[]> imprimerFacture(@PathVariable String paiementId, @RequestParam UUID clientId) {
        try {
            byte[] pdf = serviceFacture.genererFacturePdf(paiementId, clientId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=facture-" + paiementId + ".pdf")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                ("Erreur lors de la génération de la facture : " + e.getMessage()).getBytes(StandardCharsets.UTF_8)
            );
        }
    }

    // --- 3. Envoyer facture par email (PDF en pièce jointe : client + 2 adresses fixes) ---
    @RequireDroit(action = "envoyer", ressource = "paiements")
    @PostMapping("/{paiementId}/envoyer")
    public ResponseEntity<?> envoyerFacture(@PathVariable String paiementId, @RequestBody EnvoyerFactureRequest req) {
        try {
            serviceFacture.envoyerFactureParEmail(paiementId, req.clientId());
            return ResponseEntity.ok(Map.of("message", "Facture envoyée avec succès."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur lors de l'envoi : " + e.getMessage()));
        }
    }

    public record EnvoyerFactureRequest(UUID clientId) {}
}