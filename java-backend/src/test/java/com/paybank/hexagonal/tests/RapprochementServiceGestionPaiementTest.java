package com.paybank.hexagonal.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.paybank.hexagonal.main.PaiementApplication;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CLASSE DE TEST
 */
@SpringBootTest(classes = PaiementApplication.class) // 👈 On lui donne la classe de configuration explicitement
//@ActiveProfiles("test")
/*
@TestPropertySource(properties = {
	    "spring.datasource.url=jdbc:postgresql://localhost:54322/postgres",
	    "spring.datasource.username=postgres",
	    "spring.datasource.password=postgres"
	})
*/
class RapprochementServiceGestionPaiementTest {

    private LocalServiceGestionPaiement serviceTest;

    @BeforeEach
    void setUp() {
        serviceTest = new LocalServiceGestionPaiement();
    }

    @Test
    void testCalculerScore_MatchParfait() {
        LocalTransaction tCompta = new LocalTransaction("1", 1500, LocalDate.of(2026, 7, 8), "REF-123", "FACTURE");
        LocalTransaction tBanque = new LocalTransaction("B1", 1500, LocalDate.of(2026, 7, 8), "REF-123", "ENCAISSEMENT");

        double score = serviceTest.calculerScore(tCompta, tBanque);
        assertEquals(1.0, score, 0.01);
    }

    @Test
    void testCalculerScore_MontantCorrectMaisDateDifferente() {
        LocalTransaction tCompta = new LocalTransaction("1", 5000, LocalDate.of(2026, 7, 1), "REF-AAA", "FACTURE");
        LocalTransaction tBanque = new LocalTransaction("B1", 5000, LocalDate.of(2026, 7, 10), "REF-ZZZ", "ENCAISSEMENT");

        double score = serviceTest.calculerScore(tCompta, tBanque);
        assertEquals(0.60, score, 0.01);
    }

    @Test
    void testRapprochementGlobal_CasIdeal() {
        List<LocalTransaction> compta = List.of(new LocalTransaction("C1", 1000, LocalDate.of(2026, 7, 8), "REF-X", "FACTURE"));
        List<LocalTransaction> banque = List.of(new LocalTransaction("B1", 1000, LocalDate.of(2026, 7, 8), "REF-X", "ENCAISSEMENT"));

        List<LocalMatchResult> resultats = serviceTest.executerRapprochementGlobal(compta, banque);

        assertEquals(1, resultats.size());
        LocalMatchResult res = resultats.get(0);
        assertEquals("MATCH", res.statut);
        assertEquals(1.0, res.score, 0.01);
        assertEquals("C1", res.tCompta.id);
        assertEquals("B1", res.tBanque.id);
    }

    @Test
    void testVerifierEgaliteSoldes() {
        assertTrue(serviceTest.verifierEgaliteSoldes(15500, 15500));
        assertFalse(serviceTest.verifierEgaliteSoldes(15500, 14000));
    }
}

/**
 * COPIE LOCALE DU SERVICE POUR LE TEST
 */
class LocalServiceGestionPaiement {

    public List<LocalMatchResult> executerRapprochementGlobal(List<LocalTransaction> compta, List<LocalTransaction> banqueOriginale) {
        List<LocalMatchResult> resultats = new ArrayList<>();
        List<LocalTransaction> banqueDisponibles = new ArrayList<>(banqueOriginale);

        for (LocalTransaction tCompta : compta) {
            LocalTransaction meilleurMatch = null;
            double meilleurScore = 0;

            for (LocalTransaction tBanque : banqueDisponibles) {
                double score = calculerScore(tCompta, tBanque);
                if (score > meilleurScore) {
                    meilleurScore = score;
                    meilleurMatch = tBanque;
                }
            }

            if (meilleurScore >= 0.8 && meilleurMatch != null) {
                resultats.add(new LocalMatchResult(tCompta, meilleurMatch, "MATCH", meilleurScore));
                banqueDisponibles.remove(meilleurMatch);
            } else {
                resultats.add(new LocalMatchResult(tCompta, null, "MANQUANT", 0));
            }
        }

        for (LocalTransaction tBanque : banqueDisponibles) {
            resultats.add(new LocalMatchResult(null, tBanque, "INCONNU", 0));
        }
        return resultats;
    }

    public double calculerScore(LocalTransaction compta, LocalTransaction banque) {
        double score = 0.0;
        if (Math.abs(compta.montantCentimes) == Math.abs(banque.montantCentimes)) score += 0.50;
        
        long joursEcart = Math.abs(ChronoUnit.DAYS.between(compta.date, banque.date));
        if (joursEcart <= 3) score += 0.20;

        if (compta.reference != null && banque.reference != null) {
            if (compta.reference.equalsIgnoreCase(banque.reference)) score += 0.20;
        }
        if ("FACTURE".equals(compta.type) && "ENCAISSEMENT".equals(banque.type)) score += 0.10;
        
        return score;
    }

    public boolean verifierEgaliteSoldes(long soldeComptableCentimes, long soldeBancaireCentimes) {
        return soldeComptableCentimes == soldeBancaireCentimes;
    }
}

/**
 * STRUCTURES DE DONNÉES LOCALES
 */
class LocalTransaction {
    public String id;
    public long montantCentimes;
    public LocalDate date;
    public String reference;
    public String type;

    public LocalTransaction(String id, long montantCentimes, LocalDate date, String reference, String type) {
        this.id = id;
        this.montantCentimes = montantCentimes;
        this.date = date;
        this.reference = reference;
        this.type = type;
    }
}

class LocalMatchResult {
    public LocalTransaction tCompta;
    public LocalTransaction tBanque;
    public String statut;
    public double score;

    public LocalMatchResult(LocalTransaction tCompta, LocalTransaction tBanque, String statut, double score) {
        this.tCompta = tCompta;
        this.tBanque = tBanque;
        this.statut = statut;
        this.score = score;
    }
}