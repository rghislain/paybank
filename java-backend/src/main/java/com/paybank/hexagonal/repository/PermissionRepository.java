package com.paybank.hexagonal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.paybank.hexagonal.entity.PermissionEntity;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    Optional<PermissionEntity> findByRoleCibleAndRessourceAndAction(String roleCible, String ressource, String action);
}