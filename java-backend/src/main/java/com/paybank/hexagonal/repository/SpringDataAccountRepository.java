package com.paybank.hexagonal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.paybank.hexagonal.entity.CompteEntity;
import java.util.UUID;

@Repository
public interface SpringDataAccountRepository extends JpaRepository<CompteEntity, UUID> {
    // Spring Data implémente automatiquement findById, save, etc
}