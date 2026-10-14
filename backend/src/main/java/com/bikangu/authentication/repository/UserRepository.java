package com.bikangu.authentication.repository;

import com.bikangu.authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.phoneNumber = :phone")
    Optional<User> findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);

    List<User> findByIsActiveTrue();

    @Query("SELECT u FROM User u WHERE u.kycStatus = :status")
    List<User> findByKycStatus(com.bikangu.common.enums.KycStatus status);

    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts AND u.lockedUntil > :now")
    List<User> findLockedUsers(@Param("maxAttempts") int maxAttempts, @Param("now") java.time.Instant now);
}