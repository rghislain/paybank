package com.paybank.hexagonal.repository;

import com.paybank.hexagonal.entity.UserAuditLogEntity;
import com.paybank.hexagonal.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLogEntity, Long> {
}