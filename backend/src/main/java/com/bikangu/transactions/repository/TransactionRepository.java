package com.bikangu.transactions.repository;

import com.bikangu.transactions.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    Page<Transaction> findByUserIdAndTransactionType(UUID userId, com.bikangu.common.enums.TransactionType type, Pageable pageable);

    Page<Transaction> findByUserIdAndStatus(UUID userId, com.bikangu.common.enums.TransactionStatus status, Pageable pageable);

    Page<Transaction> findByFinancialAccountId(UUID accountId, Pageable pageable);

    Page<Transaction> findByVaultId(UUID vaultId, Pageable pageable);

    Page<Transaction> findByGoalId(UUID goalId, Pageable pageable);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserIdAndDateRange(@Param("userId") UUID userId, @Param("start") Instant start, @Param("end") Instant end, Pageable pageable);

    List<Transaction> findByStatusInAndRetryCountLessThan(
            List<com.bikangu.common.enums.TransactionStatus> statuses, int maxRetries);
}