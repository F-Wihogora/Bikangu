package com.bikangu.audit.repository;

import com.bikangu.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);

    Page<AuditLog> findByActorId(UUID actorId, Pageable pageable);

    Page<AuditLog> findByResourceTypeAndResourceId(String resourceType, UUID resourceId, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.correlationId = :correlationId ORDER BY a.createdAt ASC")
    List<AuditLog> findByCorrelationId(@Param("correlationId") UUID correlationId);
}