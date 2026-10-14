package com.bikangu.savings.repository;

import com.bikangu.savings.entity.SavingsRule;
import com.bikangu.savings.entity.SavingsVault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavingsRuleRepository extends JpaRepository<SavingsRule, UUID> {

    List<SavingsRule> findByUserId(UUID userId);

    List<SavingsRule> findByUserIdAndIsActiveTrueAndIsPausedFalse(UUID userId);

    List<SavingsRule> findByVaultId(UUID vaultId);

    @Query("SELECT r FROM SavingsRule r WHERE r.user.id = :userId AND r.isActive = true AND r.isPaused = false ORDER BY r.priority DESC")
    List<SavingsRule> findActiveRulesByUserOrderByPriority(@Param("userId") UUID userId);

    @Query("SELECT r FROM SavingsRule r WHERE r.vault.id = :vaultId AND r.isActive = true AND r.isPaused = false")
    List<SavingsRule> findActiveRulesByVault(@Param("vaultId") UUID vaultId);
}