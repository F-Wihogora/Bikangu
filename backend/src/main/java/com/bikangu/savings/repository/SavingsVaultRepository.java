package com.bikangu.savings.repository;

import com.bikangu.savings.entity.SavingsVault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavingsVaultRepository extends JpaRepository<SavingsVault, UUID> {

    List<SavingsVault> findByUserId(UUID userId);

    List<SavingsVault> findByUserIdAndStatusIn(UUID userId, List<com.bikangu.common.enums.VaultStatus> statuses);

    @Query("SELECT v FROM SavingsVault v WHERE v.user.id = :userId AND v.unlockDate IS NOT NULL AND v.unlockDate > :now ORDER BY v.unlockDate ASC")
    List<SavingsVault> findLockedVaultsWithUnlockDate(@Param("userId") UUID userId, @Param("now") java.time.LocalDate now);
}