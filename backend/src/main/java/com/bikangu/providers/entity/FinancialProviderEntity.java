package com.bikangu.providers.entity;

import com.bikangu.common.domain.BaseEntity;
import com.bikangu.common.enums.ProviderCategory;
import com.bikangu.common.enums.ProviderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "financial_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialProviderEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 30)
    private ProviderType providerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ProviderCategory category;

    @Column(name = "country_code", nullable = false, length = 3)
    private String countryCode;

    @Column(name = "currency_codes", columnDefinition = "TEXT[]")
    private String[] currencyCodes;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "api_base_url", length = 500)
    private String apiBaseUrl;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_sandbox", nullable = false)
    @Builder.Default
    private Boolean isSandbox = false;

    @Column(name = "supports_account_verification", nullable = false)
    @Builder.Default
    private Boolean supportsAccountVerification = true;

    @Column(name = "supports_balance_check", nullable = false)
    @Builder.Default
    private Boolean supportsBalanceCheck = true;

    @Column(name = "supports_transaction_history", nullable = false)
    @Builder.Default
    private Boolean supportsTransactionHistory = true;

    @Column(name = "supports_transfer", nullable = false)
    @Builder.Default
    private Boolean supportsTransfer = true;

    @Column(name = "supports_webhooks", nullable = false)
    @Builder.Default
    private Boolean supportsWebhooks = true;

    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_schema", columnDefinition = "JSONB")
    private String configSchema;
}