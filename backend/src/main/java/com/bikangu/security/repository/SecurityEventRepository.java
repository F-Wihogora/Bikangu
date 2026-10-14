package com.bikangu.security.repository;

import com.bikangu.security.entity.SecurityEvent;
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
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

    Page<SecurityEvent> findByUserId(UUID userId, Pageable pageable);

    Page<SecurityEvent> findByEventType(String eventType, Pageable pageable);

    Page<SecurityEvent> findBySeverity(String severity, Pageable pageable);

    Page<SecurityEvent> findByResolvedFalse(Pageable pageable);

    Page<SecurityEvent> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);

    @Query("SELECT s FROM SecurityEvent s WHERE s.ipAddress = :ip AND s.createdAt > :since ORDER BY s.createdAt DESC")
    List<SecurityEvent> findRecentByIp(@Param("ip") String ip, @Param("since") Instant since);
}