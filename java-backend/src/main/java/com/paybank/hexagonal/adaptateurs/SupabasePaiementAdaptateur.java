package com.paybank.hexagonal.adaptateurs;

import com.paybank.hexagonal.domaine.MontantCentimes;
import com.paybank.hexagonal.domaine.Transaction;
import com.paybank.hexagonal.domaine.TransactionPaiement;
import com.paybank.hexagonal.ports.PersistancePaiementSPI;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SupabasePaiementAdaptateur implements PersistancePaiementSPI {

    private final JdbcTemplate jdbcTemplate;

    public SupabasePaiementAdaptateur(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    //Le RowMapper permet de convertir le résultat SQL (PostgreSQL) vers l'Entité du Domaine
    private final RowMapper<TransactionPaiement> paiementRowMapper = (rs, rowNum) -> {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID compteClientId = UUID.fromString(rs.getString("client_id"));
        MontantCentimes montant = new MontantCentimes(rs.getInt("montant_centimes"));
        String cleIdempotence = rs.getString("cle_idempotence");
        
        // Récupération et conversion du String PostgreSQL vers l'Enum du Domaine
        TransactionPaiement.StatutTransaction statut = 
                TransactionPaiement.StatutTransaction.valueOf(rs.getString("statut"));

        return new TransactionPaiement(id, compteClientId, montant, statut, cleIdempotence);
    };

    @Override
    public Optional<TransactionPaiement> chercherParCleIdempotence(String cleIdempotence) {
        // Code SQL d'interrogation natif PostgreSQL de la table 'payments'
    	String sql = "SELECT id, client_id, montant_centimes, cle_idempotence, statut FROM paiements WHERE cle_idempotence = ?";
	   try {
	       TransactionPaiement transaction = jdbcTemplate.queryForObject(sql, paiementRowMapper, cleIdempotence);
	       return Optional.ofNullable(transaction);
	   } catch (EmptyResultDataAccessException e) {
	       // Si aucune ligne ne correspond à cette clé d'idempotence
	       return Optional.empty();
	   }
        //return Optional.empty(); // Remplacer par l'extraction et mapping rs -> TransactionPaiement
    }

    @Override
    public TransactionPaiement enregistrer(TransactionPaiement transaction) {
        // Requête PostgreSQL UPSERT native
    	// Utilisation d'un UPSERT natif PostgreSQL (ON CONFLICT) basé sur l'UUID (id)
        // ou la clé d'idempotence (si définie comme UNIQUE en BDD)
        String sql = "INSERT INTO paiements (id, client_id, montant_centimes, statut, cle_idempotence) VALUES (?::uuid, ?::uuid, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET statut = EXCLUDED.statut";

        jdbcTemplate.update(
                sql,
                transaction.getId().toString(),
                transaction.getCompteClientId().toString(),
                transaction.getMontant().getValeur(),
                transaction.getStatut().name(),
                transaction.getCleIdempotence()
        );
        return transaction;
    }

	@Override
	public void mettreAJourStatut(UUID id, TransactionPaiement.StatutTransaction nouveauStatut) {
		String sql = "UPDATE paiements SET statut = ? WHERE id = ?::uuid";
	    this.jdbcTemplate.update(sql, nouveauStatut.name(), id);		
	}

	@Override
	public void mettreAJourIDPaymentIntent(UUID id, String paymentIntentID) {
		String sql = "UPDATE paiements SET stripe_payment_intent_id = ? WHERE id = ?::uuid";
	    this.jdbcTemplate.update(sql, paymentIntentID, id);		
	}
	
	@Override
	public Optional<TransactionPaiement> chercherParPaymentIntentId(String paymentIntentId) {
	    String sql = "SELECT id, client_id, montant_centimes, cle_idempotence, statut FROM paiements WHERE stripe_payment_intent_id = ?";
	    try {
	        TransactionPaiement transaction = jdbcTemplate.queryForObject(sql, paiementRowMapper, paymentIntentId);
	        return Optional.ofNullable(transaction);
	    } catch (EmptyResultDataAccessException e) {
	        return Optional.empty();
	    }
	}
	
	@Override
	public void modifierPaiement(TransactionPaiement paiement) {
	    String sql = "UPDATE paiements SET montant_centimes = ?, statut = ? WHERE id = ?::uuid";
	    jdbcTemplate.update(sql, 
	        paiement.getMontant().getValeur(), 
	        paiement.getStatut().name(), 
	        paiement.getId()
	    );
	}

	@Override
	public Optional<TransactionPaiement> chercherParId(UUID id) {
		String sql = "SELECT id, client_id, montant_centimes, cle_idempotence, statut FROM paiements WHERE id = ?";
	    try {
	        TransactionPaiement transaction = jdbcTemplate.queryForObject(sql, paiementRowMapper, id);
	        return Optional.ofNullable(transaction);
	    } catch (EmptyResultDataAccessException e) {
	        return Optional.empty();
	    }
	}
	
	/*
	@Override
    public void creerPaiementLocal(UUID id, UUID clientId, long montant, String stripeIntentId, String statut) {
        String sql = "INSERT INTO paiements (id, client_id, montant_centimes, stripe_payment_intent_id, statut) " +
                     "VALUES (?::uuid, ?::uuid, ?, ?, ?) " +
                     "ON CONFLICT (id) DO NOTHING";
                     
        jdbcTemplate.update(sql, id.toString(), clientId.toString(), montant, stripeIntentId, statut);
    }
    */
	
	@Override
	public void creerPaiementLocal(UUID id, UUID clientId, long montantCentimes, String stripePaymentIntentId, String statut, String cleIdempotence) {
	    // On ajoute la colonne et le point d'interrogation (?) dans le INSERT
	    String sql = "INSERT INTO paiements (id, client_id, montant_centimes, stripe_payment_intent_id, statut, cle_idempotence) " +
	                 "VALUES (?::uuid, ?::uuid, ?, ?, ?, ?) " +
	                 "ON CONFLICT (id) DO NOTHING";
	                 
	    // On passe 'cleIdempotence' à la fin du jdbcTemplate
	    jdbcTemplate.update(sql, id, clientId, montantCentimes, stripePaymentIntentId, statut, cleIdempotence);
	}

    @Override
    public void mettreAJourMontantLocal(String stripeIntentId, long nouveauMontant) {
        String sql = "UPDATE paiements SET montant_centimes = ? WHERE stripe_payment_intent_id = ?";
        jdbcTemplate.update(sql, nouveauMontant, stripeIntentId);
    }

    @Override
    public void annulerPaiementLocal(String stripeIntentId) {
        String sql = "UPDATE paiements SET statut = 'CANCELLED' WHERE stripe_payment_intent_id = ?";
        jdbcTemplate.update(sql, stripeIntentId);
    }

    @Override
    public void mettreAJourStatutLocal(String stripeId, String nouveauStatut) {
        // On remplace "stripe_id" par "stripe_payment_intent_id"
        String sql = "UPDATE paiements SET statut = ? WHERE stripe_payment_intent_id = ?"; 
        jdbcTemplate.update(sql, nouveauStatut, stripeId);
    }
    
    public Map<String, Object> trouverParId(UUID id) {
        String sql = "SELECT id, client_id, montant_centimes, stripe_payment_intent_id, statut FROM paiements WHERE id = ?::uuid";
        try {
            return jdbcTemplate.queryForMap(sql, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null; // Retourne null si aucun paiement ne correspond à cet ID
        }
    }
  
    @Override
    public void mettreAJourStatut(UUID id, String statut) {
        String sql = "UPDATE paiements SET statut = ? WHERE id = ?::uuid";
        jdbcTemplate.update(sql, statut, id);
    }

    @Override
    public void enregistrerRapprochement(UUID paiementId, String stripeIntentId, String statutRapprochement) {
        String sql = "UPDATE paiements SET statut_rapprochement = ?, date_rapprochement = NOW() WHERE id = ?::uuid";
        jdbcTemplate.update(sql, statutRapprochement, paiementId);
    }

    @Override
    public void supprimerRapprochement(UUID paiementId) {
        String sql = "UPDATE paiements SET statut_rapprochement = NULL, date_rapprochement = NULL WHERE id = ?::uuid";
        jdbcTemplate.update(sql, paiementId);
    }

    @Override
	public Map<String, Object> chercherParId2(UUID id) {
		String sql = "SELECT id, client_id, montant_centimes, stripe_payment_intent_id, statut FROM paiements WHERE id = ?::uuid";
	    try {
	        return jdbcTemplate.queryForMap(sql, id);
	    } catch (org.springframework.dao.EmptyResultDataAccessException e) {
	        return null;
	    }
	}

	@Override
	public boolean estRapprochementValide(String idIntentAttendu, String idIntentReel, long montantAttenduCentimes,
			long montantReelCentimes, String stripeStatus) {
		// On délègue à une instance ou à une logique pure du domaine
	    boolean montantsEgaux = montantAttenduCentimes == montantReelCentimes;
	    boolean stripeValide = "succeeded".equals(stripeStatus);
	    return montantsEgaux && stripeValide;
	}
    
}