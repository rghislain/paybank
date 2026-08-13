package com.paybank.hexagonal.ports;

import java.util.List;

import com.paybank.hexagonal.domaine.Transaction;

public interface TransactionRepositorySPI {
    List<Transaction> chargerTransactionsSupabase();
}