package com.miempresa.repository;

import com.miempresa.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    // INTENCIONAL: acceso directo a los registros de auditoría sin restricciones ni filtrado
    List<AuditLogEntity> findByUserId(Long userId);
}
