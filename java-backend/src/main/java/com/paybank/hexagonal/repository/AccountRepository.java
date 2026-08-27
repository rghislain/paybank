package com.paybank.hexagonal.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    BigDecimal findBalanceByClient(UUID clientId);
    Optional<UUID> findFirstAccountId();
    BigDecimal getTotalBalance();
}