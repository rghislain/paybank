package com.paybank.hexagonal.port;

import java.util.UUID;
import com.paybank.hexagonal.domaine.MontantCentimes;

public interface PasserelleBancaireSPI {
    String executerTransactionBancaire(MontantCentimes montant, String tokenCarteMokbank);
    boolean annulerPaiement(String transactionId);
}