package com.paybank.hexagonal.ports;

import java.util.List;
import java.util.UUID;

import com.paybank.hexagonal.domaine.Transaction;
import com.paybank.hexagonal.domaine.TransactionDetail;

public interface TransactionRepositorySPI {
    List<Transaction> chargerTransactionsSupabase();
    void enregistrer(UUID clientId, TransactionDetail transaction);
}