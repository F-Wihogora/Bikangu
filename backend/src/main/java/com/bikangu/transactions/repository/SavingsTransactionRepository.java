package com.bikangu.transactions.repository;

import com.bikangu.transactions.entity.SavingsTransaction;
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
public interface SavingsTransactionRepository extends JpaRepository<SavingsTransaction, UUID> {

    Page<SavingsTransaction> findByUserId(UUID userId, Pageable pageable);

    Page<SavingsTransaction> findByVaultId(UUID vaultId, Pageable pageable);

    Page<SavingsTransaction> findByRuleId(UUID ruleId, Pageable pageable);

    Page<SavingsTransaction> findByGoalId(UUID goalId, Pageable pageable);

    Optional<SavingsTransaction> findByTransactionId(UUID transactionId);

    @Query("SELECT s FROM SavingsTransaction s WHERE s.user.id = :userId AND s.createdAt BETWEEN :start AND :end ORDER BY s.createdAt DESC")
    Page<SavingsTransaction> findByUserIdAndDateRange(@Param("userId") UUID userId, @Param("start") Instant start, @Param("end") Instant end, Pageable pageable);
}