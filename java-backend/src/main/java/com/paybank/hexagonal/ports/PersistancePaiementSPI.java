package com.paybank.hexagonal.ports;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.paybank.hexagonal.domaine.TransactionPaiement;

public interface PersistancePaiementSPI {
    Optional<TransactionPaiement> chercherParCleIdempotence(String cleIdempotence);
    TransactionPaiement enregistrer(TransactionPaiement transaction);
    void mettreAJourStatut(UUID id, TransactionPaiement.StatutTransaction nouveauStatut);
    void mettreAJourIDPaymentIntent(UUID id, String paymentIntentID);
	Optional<TransactionPaiement> chercherParPaymentIntentId(String paymentIntentId);
	void modifierPaiement(TransactionPaiement paiement);
	Optional<TransactionPaiement> chercherParId(UUID id);
	void creerPaiementLocal(UUID id, UUID clientId, long montant, String stripeIntentId, String statut, String clefIdempotence);
    void mettreAJourMontantLocal(String stripeIntentId, long nouveauMontant);
    void annulerPaiementLocal(String stripeIntentId);
    void mettreAJourStatutLocal(String stripeId, String nouveauStatut);
    Map<String, Object> trouverParId(UUID id);
    void mettreAJourStatut(UUID id, String statut);  
    void enregistrerRapprochement(UUID paiementId, String stripeIntentId, String statutRapprochement);
    void supprimerRapprochement(UUID paiementId);
    Map<String, Object> chercherParId2(UUID id);
    boolean estRapprochementValide(String idIntentAttendu, String idIntentReel, long montantAttenduCentimes, long montantReelCentimes, String stripeStatus);
}