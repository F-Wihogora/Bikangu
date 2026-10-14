package com.bikangu.authentication.repository;

import com.bikangu.authentication.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findByUserId(UUID userId);

    Optional<UserSession> findByUserIdAndDeviceId(UUID userId, String deviceId);

    List<UserSession> findByUserIdAndRevokedAtIsNull(UUID userId);

    Optional<UserSession> findByRefreshTokenId(UUID refreshTokenId);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.revokedAt = :now WHERE s.user.id = :userId AND s.revokedAt IS NULL")
    int revokeAllUserSessions(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.lastActivityAt = :now WHERE s.id = :sessionId")
    int updateLastActivity(@Param("sessionId") UUID sessionId, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") Instant now);
}