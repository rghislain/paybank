package com.paybank.hexagonal.repository;

import com.paybank.hexagonal.entity.PasswordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordRepository extends JpaRepository<PasswordEntity, String> {

    Optional<PasswordEntity> findByRole(String role);
    
}