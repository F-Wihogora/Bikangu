package com.bikangu.transactions.repository;

import com.bikangu.transactions.entity.IncomeTransaction;
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
public interface IncomeTransactionRepository extends JpaRepository<IncomeTransaction, UUID> {

    Page<IncomeTransaction> findByUserId(UUID userId, Pageable pageable);

    Page<IncomeTransaction> findByFinancialAccountId(UUID accountId, Pageable pageable);

    Optional<IncomeTransaction> findByExternalTransactionId(String externalTransactionId);

    List<IncomeTransaction> findByProcessedFalse();

    @Query("SELECT i FROM IncomeTransaction i WHERE i.user.id = :userId AND i.receivedAt BETWEEN :start AND :end ORDER BY i.receivedAt DESC")
    Page<IncomeTransaction> findByUserIdAndDateRange(@Param("userId") UUID userId, @Param("start") Instant start, @Param("end") Instant end, Pageable pageable);

    @Query("SELECT i FROM IncomeTransaction i WHERE i.financialAccountId = :accountId AND i.receivedAt > :since ORDER BY i.receivedAt DESC")
    List<IncomeTransaction> findRecentByAccount(@Param("accountId") UUID accountId, @Param("since") Instant since);
}