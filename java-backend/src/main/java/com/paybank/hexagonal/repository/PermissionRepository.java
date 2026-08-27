package com.paybank.hexagonal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.paybank.hexagonal.entity.Permission;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByRoleCibleAndRessourceAndAction(String roleCible, String ressource, String action);
}