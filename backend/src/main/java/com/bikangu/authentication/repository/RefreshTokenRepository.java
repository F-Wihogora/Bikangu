package com.bikangu.authentication.repository;

import com.bikangu.authentication.entity.RefreshToken;
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
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserId(UUID userId);

    List<RefreshToken> findByUserIdAndRevokedAtIsNull(UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    int revokeAllUserTokens(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now, rt.replacedByTokenId = :newTokenId WHERE rt.id = :tokenId")
    int revokeAndReplace(@Param("tokenId") UUID tokenId, @Param("now") Instant now, @Param("newTokenId") UUID newTokenId);
}