# Bikangu Provider Integration Guide

## Overview

Bikangu uses a **Provider Abstraction Layer** that allows integration with any financial service provider without modifying the core savings engine. This guide covers implementing new providers.

## Provider Interface

```java
public interface FinancialProvider {
    String getProviderId();
    String getProviderName();
    boolean isSandbox();
    
    // Account Management
    ProviderAccount verifyAccount(String accountId, String credentials);
    
    // Balance Inquiry
    ProviderAccount getBalance(String accountId);
    
    // Transaction History
    List<ProviderTransaction> getTransactions(String accountId, int limit, String cursor);
    
    // Income Detection
    List<ProviderTransaction> detectIncome(String accountId, Instant since);
    
    // Transfers
    TransferResponse initiateTransfer(TransferRequest request);
    TransferResponse getTransferStatus(String externalTransferId);
    
    // Webhooks
    void handleWebhook(String payload, String signature);
    
    // Health
    boolean isHealthy();
}
```

## Data Models

### ProviderAccount
```java
record ProviderAccount(
    UUID id,
    String providerAccountId,
    String accountType,
    String accountName,
    String accountNumberMasked,
    String accountHolderName,
    String currency,
    BigDecimal currentBalance,
    BigDecimal availableBalance,
    boolean isVerified,
    boolean isActive,
    Instant lastSyncAt,
    List<ProviderTransaction> recentTransactions
)
```

### ProviderTransaction
```java
record ProviderTransaction(
    UUID id,
    String externalTransactionId,
    String type,           // CREDIT, DEBIT
    String category,       // SALARY, FREELANCE, BONUS, etc.
    BigDecimal amount,
    String currency,
    String description,
    String reference,
    Instant transactionDate,
    String status,         // SUCCESS, PENDING, FAILED
    String incomeSource,
    String incomeCategory
)
```

### TransferRequest
```java
record TransferRequest(
    UUID idempotencyKey,
    String fromAccountId,
    String toAccountId,
    BigDecimal amount,
    String currency,
    String description,
    String reference
)
```

### TransferResponse
```java
record TransferResponse(
    UUID id,
    String externalTransferId,
    String status,         // SUCCESS, PROCESSING, FAILED
    BigDecimal amount,
    String currency,
    BigDecimal feeAmount,
    BigDecimal netAmount,
    Instant initiatedAt,
    Instant completedAt,
    String failureReason
)
```

---

## Implementing a New Provider

### 1. Create Provider Class

```java
@Component
@Slf4j
public class CustomBankProvider implements FinancialProvider {

    private final CustomBankApiClient apiClient;
    private final ProviderConfig config;

    @Override
    public String getProviderId() {
        return "CUSTOM_BANK";
    }

    @Override
    public String getProviderName() {
        return "Custom Bank";
    }

    @Override
    public boolean isSandbox() {
        return config.isSandbox();
    }

    @Override
    public ProviderAccount verifyAccount(String accountId, String credentials) {
        // 1. Call provider API to verify account
        // 2. Map response to ProviderAccount
        // 3. Handle provider-specific errors
        
        CustomBankAccountResponse response = apiClient.verifyAccount(accountId, credentials);
        
        return new ProviderAccount(
            UUID.randomUUID(),
            response.getAccountId(),
            response.getAccountType(),
            response.getAccountName(),
            maskAccountNumber(response.getAccountNumber()),
            response.getAccountHolderName(),
            response.getCurrency(),
            response.getCurrentBalance(),
            response.getAvailableBalance(),
            true,
            true,
            Instant.now(),
            List.of()
        );
    }

    @Override
    public ProviderAccount getBalance(String accountId) {
        CustomBankBalanceResponse response = apiClient.getBalance(accountId);
        
        return new ProviderAccount(
            UUID.randomUUID(),
            accountId,
            null,
            response.getAccountName(),
            maskAccountNumber(response.getAccountNumber()),
            null,
            response.getCurrency(),
            response.getCurrentBalance(),
            response.getAvailableBalance(),
            true,
            true,
            Instant.now(),
            List.of()
        );
    }

    @Override
    public List<ProviderTransaction> getTransactions(String accountId, int limit, String cursor) {
        CustomBankTransactionResponse response = apiClient.getTransactions(accountId, limit, cursor);
        
        return response.getTransactions().stream()
            .map(this::mapToProviderTransaction)
            .toList();
    }

    @Override
    public List<ProviderTransaction> detectIncome(String accountId, Instant since) {
        // Filter for credit transactions that qualify as income
        List<ProviderTransaction> all = getTransactions(accountId, 100, null);
        
        return all.stream()
            .filter(t -> "CREDIT".equals(t.type()))
            .filter(t -> t.transactionDate().isAfter(since))
            .filter(this::isQualifyingIncome)
            .toList();
    }

    @Override
    public TransferResponse initiateTransfer(TransferRequest request) {
        // 1. Generate idempotency key
        // 2. Call provider transfer API
        // 3. Handle async/sync response
        // 4. Map to TransferResponse
        
        CustomBankTransferRequest apiRequest = new CustomBankTransferRequest(
            request.idempotencyKey().toString(),
            request.fromAccountId(),
            request.toAccountId(),
            request.amount(),
            request.currency(),
            request.description(),
            request.reference()
        );
        
        CustomBankTransferResponse response = apiClient.initiateTransfer(apiRequest);
        
        return mapToTransferResponse(response);
    }

    @Override
    public TransferResponse getTransferStatus(String externalTransferId) {
        CustomBankTransferStatusResponse response = apiClient.getTransferStatus(externalTransferId);
        return mapToTransferResponse(response);
    }

    @Override
    public void handleWebhook(String payload, String signature) {
        // 1. Verify signature
        if (!verifySignature(payload, signature)) {
            throw new SecurityException("Invalid webhook signature");
        }
        
        // 2. Parse event
        CustomBankWebhookEvent event = parseWebhook(payload);
        
        // 3. Process based on event type
        switch (event.getEventType()) {
            case "transaction.created" -> processTransactionEvent(event);
            case "transfer.completed" -> processTransferEvent(event);
            case "transfer.failed" -> processFailedTransfer(event);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            return apiClient.healthCheck();
        } catch (Exception e) {
            log.warn("Health check failed for {}", getProviderId(), e);
            return false;
        }
    }

    // Helper methods...
    private boolean verifySignature(String payload, String signature) {
        // Implement HMAC verification
        return true;
    }

    private boolean isQualifyingIncome(ProviderTransaction t) {
        // Business logic for income detection
        return true;
    }
}
```

### 2. Create API Client

```java
@Component
public class CustomBankApiClient {
    
    private final WebClient webClient;
    private final ProviderConfig config;

    public CustomBankApiClient(ProviderConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
            .baseUrl(config.getBaseUrl())
            .defaultHeader("Authorization", "Bearer " + config.getApiKey())
            .defaultHeader("Content-Type", "application/json")
            .filter(logRequest())
            .filter(logResponse())
            .build();
    }

    public CustomBankAccountResponse verifyAccount(String accountId, String credentials) {
        return webClient.post()
            .uri("/accounts/{id}/verify", accountId)
            .bodyValue(new VerifyRequest(credentials))
            .retrieve()
            .bodyToMono(CustomBankAccountResponse.class)
            .block();
    }
    
    // Other API methods...
}
```

### 3. Register Provider

```java
@Configuration
public class ProviderConfig {
    
    @Bean
    public void registerProviders(ProviderRegistry registry, 
                                  CustomBankProvider customBankProvider) {
        registry.register(customBankProvider);
    }
}
```

### 4. Add Database Migration

```sql
-- V3__add_custom_bank_provider.sql
INSERT INTO financial_providers (
    name, display_name, provider_type, category, country_code, 
    currency_codes, logo_url, icon_url, website_url, 
    is_active, is_sandbox, supports_account_verification,
    supports_balance_check, supports_transaction_history,
    supports_transfer, supports_webhooks, rate_limit_per_minute,
    config_schema
) VALUES (
    'CUSTOM_BANK', 'Custom Bank', 'BANK', 'COMMERCIAL_BANK', 'RW',
    ARRAY['RWF'], 
    'https://assets.bikangu.com/providers/custom-bank-logo.png',
    'https://assets.bikangu.com/providers/custom-bank-icon.png',
    'https://custombank.com',
    true, false, true, true, true, true, true, 30,
    '{"apiVersion": "v1", "environment": "production", "requiredFields": ["accountNumber", "pin"]}'::jsonb
);
```

---

## Provider-Specific Considerations

### MTN Mobile Money (Rwanda)
- **Auth**: API Key + API Secret
- **Account ID**: Phone number (2507xxxxxxxx)
- **Credentials**: User PIN
- **Webhook**: `transaction.created`, `transfer.completed`
- **Rate Limit**: 60 req/min
- **Currency**: RWF only

### Airtel Money (Rwanda)
- **Auth**: Client ID + Client Secret (OAuth2)
- **Account ID**: Phone number
- **Credentials**: User PIN
- **Webhook**: Similar to MTN
- **Rate Limit**: 60 req/min

### Commercial Banks
- **Auth**: API Key or Certificate-based
- **Account ID**: Account number
- **Credentials**: PIN/Password + OTP
- **Webhook**: Varies by bank
- **Rate Limit**: 30 req/min typical

### Open Banking
- **Standard**: Open Banking UK / PSD2 / FAPI
- **Auth**: OAuth2 + mTLS
- **Consent**: Explicit user consent required
- **Scopes**: `accounts`, `transactions`, `payments`
- **Rate Limit**: Per TPP agreement

---

## Testing Providers

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class CustomBankProviderTest {

    @Mock
    private CustomBankApiClient apiClient;
    
    @InjectMocks
    private CustomBankProvider provider;

    @Test
    void verifyAccount_shouldReturnProviderAccount() {
        // Given
        when(apiClient.verifyAccount(any(), any()))
            .thenReturn(mockAccountResponse());

        // When
        ProviderAccount result = provider.verifyAccount("123456", "1234");

        // Then
        assertThat(result.providerAccountId()).isEqualTo("123456");
        assertThat(result.isVerified()).isTrue();
    }

    @Test
    void initiateTransfer_shouldReturnTransferResponse() {
        // Given
        when(apiClient.initiateTransfer(any()))
            .thenReturn(mockTransferResponse());

        // When
        TransferResponse result = provider.initiateTransfer(mockTransferRequest());

        // Then
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.externalTransferId()).isNotNull();
    }
}
```

### Integration Tests
```java
@SpringBootTest
@Testcontainers
class CustomBankProviderIntegrationTest {

    @Container
    static GenericContainer<?> mockServer = new GenericContainer<>("wiremock/wiremock:2.35")
        .withExposedPorts(8080);

    @Autowired
    private CustomBankProvider provider;

    @Test
    void fullTransferFlow() {
        // Configure WireMock
        // Test end-to-end flow
    }
}
```

### Contract Testing
- Use **Pact** for consumer-driven contracts
- Verify provider API compatibility
- Run in CI pipeline

---

## Webhook Security

### Signature Verification
```java
private boolean verifySignature(String payload, String signature) {
    String expectedSignature = HmacUtils.hmacSha256(
        config.getWebhookSecret(), 
        payload.getBytes(StandardCharsets.UTF_8)
    );
    return MessageDigest.isEqual(
        expectedSignature.getBytes(StandardCharsets.UTF_8),
        signature.getBytes(StandardCharsets.UTF_8)
    );
}
```

### Replay Protection
- Timestamp in payload (reject if > 5 minutes old)
- Nonce/idempotency key tracking
- Store processed event IDs with TTL

### Idempotency
```java
@EventListener
public void handleWebhook(WebhookEvent event) {
    String idempotencyKey = event.getProviderId() + ":" + event.getExternalEventId();
    
    // Check if already processed
    if (webhookEventRepository.existsByIdempotencyKey(idempotencyKey)) {
        log.info("Duplicate webhook ignored: {}", idempotencyKey);
        return;
    }
    
    // Process and store
    webhookEventRepository.save(new WebhookEventEntity(
        event.getProviderId(),
        event.getExternalEventId(),
        idempotencyKey,
        event.getPayload()
    ));
    
    processEvent(event);
}
```

---

## Monitoring Provider Health

### Health Endpoint
```java
@Component
public class ProviderHealthIndicator implements HealthIndicator {

    private final ProviderRegistry registry;

    @Override
    public Health health() {
        Map<String, Boolean> providerStatus = new HashMap<>();
        
        for (FinancialProvider provider : registry.getAllProviders()) {
            try {
                boolean healthy = provider.isHealthy();
                providerStatus.put(provider.getProviderId(), healthy);
            } catch (Exception e) {
                providerStatus.put(provider.getProviderId(), false);
            }
        }
        
        boolean allHealthy = providerStatus.values().stream().allMatch(Boolean::booleanValue);
        
        return allHealthy 
            ? Health.up().withDetails(providerStatus).build()
            : Health.down().withDetails(providerStatus).build();
    }
}
```

### Metrics
```prometheus
# Provider health
provider_healthy{provider="MTN_MOMO"} 1
provider_healthy{provider="AIRTEL_MONEY"} 1

# API latency
provider_api_latency_seconds{provider="MTN_MOMO",endpoint="verify"} 0.234

# Error rate
provider_api_errors_total{provider="MTN_MOMO",endpoint="transfer"} 2

# Webhook processing
provider_webhook_processed_total{provider="MTN_MOMO",status="success"} 145
provider_webhook_processed_total{provider="MTN_MOMO",status="failed"} 3
```

---

## Sandbox/Development

### Mock Provider
```java
@Component
@Profile("dev")
public class MockFinancialProvider implements FinancialProvider {
    // Simulates all provider operations
    // Configurable scenarios: SUCCESS, INSUFFICIENT_FUNDS, TIMEOUT, FAILED
    
    public void simulateSalaryReceived(String accountId, BigDecimal amount, String source) {
        // Creates mock income transaction
        // Triggers savings engine
    }
    
    public void setSimulationScenario(String idempotencyKey, Scenario scenario) {
        // Configure transfer behavior for testing
    }
}
```

### Test Scenarios
| Scenario | Description |
|----------|-------------|
| `SUCCESS` | Normal successful transfer |
| `INSUFFICIENT_FUNDS` | Balance too low |
| `PROVIDER_TIMEOUT` | Simulated network timeout |
| `TRANSFER_FAILED` | Provider returns error |

---

## Deployment Checklist

- [ ] Provider credentials stored in Vault/Secrets Manager
- [ ] Webhook URLs configured and accessible
- [ ] SSL certificates valid for provider endpoints
- [ ] Rate limits configured per provider contract
- [ ] Monitoring alerts configured
- [ ] Contract tests passing
- [ ] Sandbox testing completed
- [ ] Rollback plan documented
- [ ] Incident response contacts updated