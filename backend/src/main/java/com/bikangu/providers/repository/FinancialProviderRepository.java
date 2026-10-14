package com.bikangu.providers.repository;

import com.bikangu.providers.entity.FinancialProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialProviderRepository extends JpaRepository<FinancialProviderEntity, UUID> {

    Optional<FinancialProviderEntity> findByName(String name);

    List<FinancialProviderEntity> findByIsActiveTrue();

    List<FinancialProviderEntity> findByCountryCode(String countryCode);

    List<FinancialProviderEntity> findByProviderType(com.bikangu.common.enums.ProviderType type);
}