package com.paybank.hexagonal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paybank.hexagonal.entity.TransactionEntity;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
	// Pour ton bilan
    List<TransactionEntity> findByClientIdAndDateBetween(UUID clientId, LocalDate start, LocalDate end);
    
    // Pour ton dashboard (count)
    long countByClientIdAndStatus(UUID clientId, String status);
    
    // Pour tes activités récentes (avec limitation)
    List<TransactionEntity> findByClientIdOrderByDateDesc(UUID clientId, org.springframework.data.domain.Pageable pageable);
}