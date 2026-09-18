package com.paybank.hexagonal.jpaRepository;

import com.paybank.hexagonal.domaine.service.FactureService;
import com.paybank.hexagonal.entite.FactureEntity;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataFactureRepository extends JpaRepository<FactureEntity, String> {
	boolean existsByPaiementsId(String paiementId);
}