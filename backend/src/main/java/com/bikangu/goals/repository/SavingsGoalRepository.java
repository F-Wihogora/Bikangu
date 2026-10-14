package com.bikangu.goals.repository;

import com.bikangu.goals.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {

    List<SavingsGoal> findByUserId(UUID userId);

    List<SavingsGoal> findByUserIdAndStatusIn(UUID userId, List<com.bikangu.common.enums.GoalStatus> statuses);

    List<SavingsGoal> findByVaultId(UUID vaultId);

    @Query("SELECT g FROM SavingsGoal g WHERE g.user.id = :userId AND g.deadline IS NOT NULL AND g.deadline >= :now AND g.deadline <= :future ORDER BY g.deadline ASC")
    List<SavingsGoal> findUpcomingDeadlines(@Param("userId") UUID userId, @Param("now") LocalDate now, @Param("future") LocalDate future);

    @Query("SELECT g FROM SavingsGoal g WHERE g.user.id = :userId AND g.status = 'ACTIVE' AND g.currentAmount >= g.targetAmount")
    List<SavingsGoal> findCompletedGoals(@Param("userId") UUID userId);
}