package com.paybank.hexagonal.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.paybank.hexagonal.domaine.model.MontantCentimes;
import com.paybank.hexagonal.main.PaiementApplication;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PaiementApplication.class)
class MontantCentimesTest {

    @Test
    void doitCreerMontantEtConvertirEnDecimal() {
        MontantCentimes montant = new MontantCentimes(2500); //25.00 EUR
        assertEquals(2500L, montant.getValeur());
        
        BigDecimal decimal = BigDecimal.valueOf(montant.getValeur()).movePointLeft(2);
        assertEquals(0, new BigDecimal("25.00").compareTo(decimal));
    }

    @Test
    void doitGererLesOperationsDeMontant() {
        MontantCentimes m1 = new MontantCentimes(1000);
        MontantCentimes m2 = new MontantCentimes(500);      
        int somme = m1.getValeur()+m2.getValeur();
        assertEquals(1500, somme);
    }

    @Test
    void neDoitPasAccepterDeMontantNegatif() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MontantCentimes(-100);
        });
    }
}