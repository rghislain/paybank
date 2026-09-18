package com.paybank.hexagonal.sortie.port;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.paybank.hexagonal.domaine.model.MontantCentimes;
import com.paybank.hexagonal.domaine.model.Transaction;
import com.stripe.exception.StripeException;

public interface StripeMokankPaiementSPI {
    String executerTransactionBancaire(MontantCentimes montant, String tokenCarteMokbank);
    boolean annulerPaiement(String transactionId);
    Map<String, String> creerIntentionPaiement(UUID id, long montantCentimes);
    String intentionPaiement(UUID id, long montantCentimes);
    void annulerIntentionPaiement(String paymentIntentId);
    Map<String, Object> obtenirSoldeCompte() throws StripeException;
    String obtenirStatutPaiement(String paymentIntentId);
    List<Transaction> chargerTransactionsStripe() throws StripeException;
	void modifierMontantPaiement(String stripePaymentIntentId, long nouveauMontantCentimes) throws StripeException;
	void synchroniserStatutPaiement(String stripePaymentIntentId) throws StripeException;
	void effectuerRapprochement(UUID paiementId, String stripePaymentIntentId) throws StripeException;
	void annulerRapprochement(UUID paiementId);
}