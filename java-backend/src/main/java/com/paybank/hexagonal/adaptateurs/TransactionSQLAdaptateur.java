package com.paybank.hexagonal.adaptateurs;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
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

	/*
	@Override
	public Optional<TransactionDetail> findById(UUID id) {
	    String sql = "SELECT id, client_id, date, amount, is_debit FROM transactions WHERE id = ?::uuid";
	    try {
	        TransactionDetail detail = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new TransactionDetail(), id);	        
	        return Optional.of(detail);
	    } catch (EmptyResultDataAccessException e) {
	        return Optional.empty();
	    }
	}

	@Override
	public List<TransactionDetail> findTransactionsByDateRange(UUID clientId, LocalDate start, LocalDate end) {
	    String sql = "SELECT id, client_id, date, amount, is_debit FROM transactions WHERE client_id = ?::uuid AND date BETWEEN ? AND ?";
	    return jdbcTemplate.query(sql, (rs, rowNum) -> new TransactionDetail(
	    ), clientId, java.sql.Date.valueOf(start), java.sql.Date.valueOf(end));
	}
	*/
	
	@Override
	public Optional<TransactionDetail> findById(UUID id) {
	    String sql = "SELECT id, client_id, date, description, amount, is_debit, reconciliation_status FROM transactions WHERE id = ?::uuid";
	    try {
	        TransactionDetail detail = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new TransactionDetail(
	            UUID.fromString(rs.getString("id")),
	            rs.getDate("date").toLocalDate(),
	            rs.getString("description"),
	            rs.getBigDecimal("amount"),
	            rs.getBoolean("is_debit"),
	            rs.getString("reconciliation_status")
	        ), id);
	        return Optional.of(detail);
	    } catch (EmptyResultDataAccessException e) {
	        return Optional.empty();
	    }
	}

	@Override
	public List<TransactionDetail> findTransactionsByDateRange(UUID clientId, LocalDate start, LocalDate end) {
	    String sql = "SELECT id, client_id, date, description, amount, is_debit, reconciliation_status FROM transactions WHERE client_id = ?::uuid AND date BETWEEN ? AND ?";
	    return jdbcTemplate.query(sql, (rs, rowNum) -> new TransactionDetail(
	        UUID.fromString(rs.getString("id")),
	        rs.getDate("date").toLocalDate(),
	        rs.getString("description"),
	        rs.getBigDecimal("amount"),
	        rs.getBoolean("is_debit"),
	        rs.getString("reconciliation_status")
	    ), clientId, java.sql.Date.valueOf(start), java.sql.Date.valueOf(end));
	}
	

}