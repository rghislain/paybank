package com.paybank.hexagonal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paybank.hexagonal.entity.RessourcesEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RessourcesRepository extends JpaRepository<RessourcesEntity, UUID> {
    
    // Permet de retrouver les ressources liées à un ID utilisateur (ex: "admin@paybank.com")
    Optional<RessourcesEntity> findByUtilisateursId(String utilisateursId);
}