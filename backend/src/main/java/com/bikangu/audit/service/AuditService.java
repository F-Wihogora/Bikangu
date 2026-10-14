package com.bikangu.audit.service;

import com.bikangu.audit.entity.AuditLog;
import com.bikangu.audit.repository.AuditLogRepository;
import com.bikangu.common.enums.AuditAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(UUID userId, UUID actorId, AuditAction action, String resourceType, UUID resourceId,
                    Object oldValues, Object newValues, String ipAddress, String userAgent, String correlationId) {
        try {
            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .actorId(actorId)
                    .action(action.name())
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .oldValues(oldValues != null ? toJson(oldValues) : null)
                    .newValues(newValues != null ? toJson(newValues) : null)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .correlationId(correlationId != null ? UUID.fromString(correlationId) : null)
                    .status("SUCCESS")
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }

    @Transactional
    public void logError(UUID userId, UUID actorId, AuditAction action, String resourceType, UUID resourceId,
                         String errorMessage, String ipAddress, String userAgent, String correlationId) {
        try {
            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .actorId(actorId)
                    .action(action.name())
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .correlationId(correlationId != null ? UUID.fromString(correlationId) : null)
                    .status("FAILED")
                    .errorMessage(errorMessage)
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}