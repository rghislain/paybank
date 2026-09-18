package com.paybank.hexagonal.test;

import com.paybank.hexagonal.domaine.model.TransactionPaiement;
import com.paybank.hexagonal.domaine.service.PaiementService;
import com.paybank.hexagonal.main.PaiementApplication;
import com.paybank.hexagonal.sortie.port.PaiementSPI;
import com.paybank.hexagonal.sortie.port.StripeMokankPaiementSPI;
import com.paybank.hexagonal.sortie.port.TransactionSPI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = PaiementApplication.class)
class ServicePaiementTest {

    private PaiementSPI paiementSPI;
    private TransactionSPI transactionSPI;
    private StripeMokankPaiementSPI stripeMokankPaiementSPI;
    private PaiementService paiementService;
    
    @BeforeEach
    void setUp() {
        paiementSPI = mock(PaiementSPI.class);
        transactionSPI = mock(TransactionSPI.class);
        stripeMokankPaiementSPI = mock(StripeMokankPaiementSPI.class);
                
        // Initialisation du service avec ses ports mockés
        paiementService = new PaiementService(paiementSPI, transactionSPI, stripeMokankPaiementSPI);
    }

    @Test
    void doitTraiterPaiementAvecSucces() {
        UUID clientId = UUID.randomUUID();
        int montant = 3000;
        String cleIdempotence = "idemp-123";
        String tokenCarte = "tok_visa";

        // Simulation du comportement de Stripe
        when(stripeMokankPaiementSPI.executerTransactionBancaire(any(), eq(tokenCarte)))
            .thenReturn("pi_123456789");

        TransactionPaiement resultat = paiementService.traiterPaiement(clientId, montant, cleIdempotence, tokenCarte);

        assertNotNull(resultat);
        assertEquals(TransactionPaiement.StatutTransaction.SUCCESS, resultat.getStatut());
        verify(paiementSPI, times(2)).enregistrer(any(TransactionPaiement.class));
    }

    @Test
    void doitGererEchecPaiementQuandStripeRejete() {
        UUID clientId = UUID.randomUUID();
        int montant = 1500;
        String cleIdempotence = "idemp-fail";
        String tokenCarte = "tok_chargeDeclinedInsufficientFunds";

        // Simulation d'une exception levée par l'adaptateur Stripe
        when(stripeMokankPaiementSPI.executerTransactionBancaire(any(), eq(tokenCarte)))
            .thenThrow(new RuntimeException("Fonds insuffisants"));

        TransactionPaiement resultat = paiementService.traiterPaiement(clientId, montant, cleIdempotence, tokenCarte);

        assertNotNull(resultat);
        assertEquals(TransactionPaiement.StatutTransaction.FAILED, resultat.getStatut());
    }
}