package com.paybank.hexagonal.adaptateurs;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.paybank.hexagonal.domaine.Transaction;
import com.paybank.hexagonal.domaine.TransactionDetail;
import com.paybank.hexagonal.ports.TransactionRepositorySPI;

@Repository
public class TransactionSQLAdaptateur implements TransactionRepositorySPI {

	private final JdbcTemplate jdbcTemplate;

	public TransactionSQLAdaptateur(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
    }
	
	@Override
	public List<Transaction> chargerTransactionsSupabase() {
		String sql = "SELECT id, client_id, montant_centimes, devise, stripe_payment_intent_id, statut, cree_le, cle_idempotence FROM paiements";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Transaction(
        		rs.getString("id"),
                rs.getLong("montant_centimes"),
                rs.getDate("cree_le").toLocalDate(), // Conversion SQL Date -> LocalDate
                rs.getString("statut"),
                rs.getString("cle_idempotence")
        ));
	}
	
	@Override
	public void enregistrer(UUID clientId, TransactionDetail transaction) {
        String sql = "INSERT INTO transactions (id, client_id, date, description, amount, is_debit, reconciliation_status) " +
                     "VALUES (?::uuid, ?::uuid, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            transaction.id(),
            clientId,
            transaction.date(),
            transaction.description(),
            transaction.amount(),
            transaction.isDebit(),
            transaction.reconciliationStatus()
        );
    }

}