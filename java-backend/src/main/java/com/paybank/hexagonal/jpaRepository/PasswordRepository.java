package com.paybank.hexagonal.jpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paybank.hexagonal.entite.PasswordEntity;

import java.util.Optional;

@Repository
public interface PasswordRepository extends JpaRepository<PasswordEntity, String> {
    Optional<PasswordEntity> findByRole(String role);
}