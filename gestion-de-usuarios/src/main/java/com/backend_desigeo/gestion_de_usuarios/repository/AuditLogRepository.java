package com.backend_desigeo.gestion_de_usuarios.repository;

import com.backend_desigeo.gestion_de_usuarios.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Filtro combinado: level y eventType opcionales
    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:level IS NULL OR a.level = :level)
          AND (:eventType IS NULL OR a.eventType = :eventType)
        ORDER BY a.createdAt DESC
        """)
    Page<AuditLog> findWithFilters(
        @Param("level") String level,
        @Param("eventType") String eventType,
        Pageable pageable
    );

    // Conteo por nivel para los KPIs
    long countByLevel(String level);
}