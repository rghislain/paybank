package com.paybank.hexagonal.jpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.paybank.hexagonal.entite.TransactionEntity;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
	//Pour le bilan
    List<TransactionEntity> findByClientIdAndDateBetween(UUID clientId, LocalDate start, LocalDate end);  
    //Pour le dashboard (count)
    long countByClientIdAndStatus(UUID clientId, String status);
    //Pour les activités récentes (avec limitation)
    List<TransactionEntity> findByClientIdOrderByDateDesc(UUID clientId, org.springframework.data.domain.Pageable pageable);
   
    @Query("SELECT t FROM TransactionEntity t WHERE t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC")
    List<TransactionEntity> findAllByDateRangeSorted(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
}