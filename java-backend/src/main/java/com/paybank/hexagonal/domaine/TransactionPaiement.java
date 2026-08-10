package com.paybank.hexagonal.domaine;

import java.util.UUID;

public class TransactionPaiement {
    private final UUID id;
    private final UUID compteClientId;
    private MontantCentimes montant;
    private final String cleIdempotence;
    private StatutTransaction statut;
    private String stripe_payment_intent_id;

    public enum StatutTransaction {
        PENDING, SUCCESS, FAILED, REFUNDED, CANCELLED
    }

    // Constructeur pour une nouvelle transaction
    public TransactionPaiement(UUID compteClientId, MontantCentimes montant, String cleIdempotence) {
        this.id = UUID.randomUUID();
        this.compteClientId = compteClientId;
        this.montant = montant;
        this.cleIdempotence = cleIdempotence;
        this.statut = StatutTransaction.PENDING;
		this.stripe_payment_intent_id = "";
    }

    // Reconstitution depuis la base de données (Supabase)
    public TransactionPaiement(UUID id, UUID compteClientId, MontantCentimes montant, StatutTransaction statut, String cleIdempotence) {
        this.id = id;
        this.compteClientId = compteClientId;
        this.montant = montant;
        this.cleIdempotence = cleIdempotence;
        this.statut = statut;
		this.stripe_payment_intent_id = "";
    }
    
    public TransactionPaiement(UUID id, UUID compteClientId, MontantCentimes montant, StatutTransaction statut, String cleIdempotence, String stripe_payment_intent_id) {
        this.id = id;
        this.compteClientId = compteClientId;
        this.montant = montant;
        this.cleIdempotence = cleIdempotence;
        this.statut = statut;
		this.stripe_payment_intent_id = stripe_payment_intent_id;
    }

    public void valider() {
        if (this.statut != StatutTransaction.PENDING) {
            throw new IllegalStateException("Impossible de valider une transaction qui n'est pas en attente.");
        }
        this.statut = StatutTransaction.SUCCESS;
    }

    public void echouer() {
        if (this.statut != StatutTransaction.PENDING) {
            throw new IllegalStateException("Impossible de faire échouer une transaction qui n'est pas en attente.");
        }
        this.statut = StatutTransaction.FAILED;
    }
    
    public void modifierMontant(MontantCentimes nouveauMontantCentimes) {
    	if(this.statut==StatutTransaction.SUCCESS) {
    		throw new IllegalStateException("impossible de modifier le montant d'un reglement success");
    	}
    	this.montant=nouveauMontantCentimes;
    }

    public UUID getId() { return id; }
    public UUID getCompteClientId() { return compteClientId; }
    public MontantCentimes getMontant() { return montant; }
    public StatutTransaction getStatut() { return statut; }
    public String getCleIdempotence() { return cleIdempotence; }
    public String getStripe_payment_intent_id() { return stripe_payment_intent_id; }

    public void setStripe_payment_intent_id(String stripe_payment_intent_id) {
    	this.stripe_payment_intent_id=stripe_payment_intent_id;
    }

}