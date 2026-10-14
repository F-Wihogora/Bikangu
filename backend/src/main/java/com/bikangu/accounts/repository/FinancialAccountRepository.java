package com.bikangu.accounts.repository;

import com.bikangu.accounts.entity.FinancialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, UUID> {

    List<FinancialAccount> findByUserId(UUID userId);

    List<FinancialAccount> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<FinancialAccount> findByUserIdAndIsPrimaryTrue(UUID userId);

    Optional<FinancialAccount> findByUserIdAndProviderId(UUID userId, UUID providerId);

    @Query("SELECT a FROM FinancialAccount a WHERE a.user.id = :userId AND a.provider.id = :providerId AND a.providerAccountId = :providerAccountId")
    Optional<FinancialAccount> findByUserProviderAndAccountId(
            @Param("userId") UUID userId,
            @Param("providerId") UUID providerId,
            @Param("providerAccountId") String providerAccountId
    );

    List<FinancialAccount> findByProviderId(UUID providerId);

    List<FinancialAccount> findByIsVerifiedTrue();
}