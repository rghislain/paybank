package com.paybank.hexagonal.ports;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.paybank.hexagonal.domaine.Transaction;
import com.paybank.hexagonal.domaine.TransactionDetail;

public interface TransactionRepositorySPI {
    List<Transaction> chargerTransactionsSupabase();
    void enregistrer(UUID clientId, TransactionDetail transaction);
    Optional<TransactionDetail> findById(UUID id);
	List<TransactionDetail> findTransactionsByDateRange(UUID clientId, LocalDate start, LocalDate end);
}