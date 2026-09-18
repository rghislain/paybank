package com.paybank.hexagonal.jpaRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository {
    BigDecimal findBalanceByClient(UUID clientId);
    Optional<UUID> findFirstAccountId();
    BigDecimal getTotalBalance();
}