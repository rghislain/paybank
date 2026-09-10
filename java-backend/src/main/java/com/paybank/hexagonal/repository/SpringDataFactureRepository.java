package com.paybank.hexagonal.repository;

import com.paybank.hexagonal.domaine.service.FactureService;
import com.paybank.hexagonal.entity.FactureEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFactureRepository extends JpaRepository<FactureEntity, String> {
	boolean existsByPaiementsId(String paiementId);
}