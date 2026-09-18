package com.paybank.hexagonal.jpaRepository;

import com.paybank.hexagonal.entite.UserAuditLogEntity;
import com.paybank.hexagonal.jpaRepository.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLogEntity, Long> {
}