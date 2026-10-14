# Bikangu Architecture Documentation

## System Overview

Bikangu follows a **Clean Architecture** with **Domain-Driven Design** principles, separating concerns into distinct layers:

```
��─────────────────────────────────────────────────────────────��
│                    Presentation Layer                          │
│  Flutter Mobile App  │  React Admin Dashboard  │  Webhooks   │
��─────────────────────────────────────────────────────────────��
                              │
                              ��
��─────────────────────────────────────────────────────────────��
│                      API Gateway Layer                         │
│              Spring Boot REST Controllers                      │
��─────────────────────────────────────────────────────────────��
                              │
                              ��
��─────────────────────────────────────────────────────────────��
│                      Application Layer                         │
│  Use Cases │ Services │ Domain Events │ Sagas │ Orchestrators │
��─────────────────────────────────────────────────────────────��
                              │
                              ��
��─────────────────────────────────────────────────────────────��
│                       Domain Layer                             │
│  Entities │ Value Objects │ Domain Services │ Repository Ifaces│
��─────────────────────────────────────────────────────────────��
                              │
                              ��
��─────────────────────────────────────────────────────────────��
│                     Infrastructure Layer                       │
│  JPA Repositories │ Provider Adapters │ External APIs │ DB    │
��─────────────────────────────────────────────────────────────��
```

## Core Domains

### 1. Authentication Domain
- **Entities**: User, Role, Permission, RefreshToken, UserSession
- **Services**: AuthService, JwtService, OtpService, EmailService
- **Security**: BCrypt(12), JWT RS256, Refresh token rotation, Device fingerprinting

### 2. Financial Accounts Domain
- **Entities**: FinancialAccount, FinancialProvider
- **Abstraction**: `FinancialProvider` interface with multiple implementations
- **Providers**: MTN MoMo, Airtel Money, Banks, MockProvider (dev)

### 3. Savings Domain
- **Entities**: SavingsVault, SavingsRule, SavingsGoal
- **Engine**: `SavingsEngine` - processes income, calculates savings, executes transfers
- **Rules**: Percentage, Fixed Amount, Conditional Percentage, Income Source, Bonus
- **Safety**: Idempotency keys, Optimistic locking, State machines

### 4. Transactions Domain
- **Entities**: Transaction, IncomeTransaction, SavingsTransaction
- **Categories**: Income, Savings, Withdrawal, Transfer, Refund, Fee, Interest, Penalty
- **States**: Pending, Processing, Success, Failed, Reversed, Cancelled

### 5. Notification Domain
- **Channels**: In-App, Push (FCM), Email, SMS
- **Types**: 20+ notification types
- **Preferences**: Per-user, per-channel, quiet hours

## Provider Abstraction

```java
public interface FinancialProvider {
    String getProviderId();
    ProviderAccount verifyAccount(String accountId, String credentials);
    ProviderAccount getBalance(String accountId);
    List<ProviderTransaction> getTransactions(String accountId, int limit, String cursor);
    List<ProviderTransaction> detectIncome(String accountId, Instant since);
    TransferResponse initiateTransfer(TransferRequest request);
    TransferResponse getTransferStatus(String externalTransferId);
    void handleWebhook(String payload, String signature);
    boolean isHealthy();
}
```

**Implemented Providers:**
- `MockFinancialProvider` - Development/testing
- `MtnMomoProvider` - Production (MTN Mobile Money)
- `AirtelMoneyProvider` - Production (Airtel Money)
- `BankProvider` - Generic bank integration
- `OpenBankingProvider` - Open Banking standard

## Savings Engine Flow

```
Income Transaction Received
         │
         ��
��────────────────────────��
│  Idempotency Check     │──Already processed?──�� Skip
��────────────────────────��
         │
         ��
��────────────────────────��
│  Find Applicable Rules │
│  (Priority Ordered)    │
��────────────────────────��
         │
         ��
��────────────────────────��
│  Calculate Savings     │
│  Per Rule              │
��────────────────────────��
         │
         ��
��────────────────────────��
│  Check Balance         │──Insufficient?──�� Fail Gracefully
��────────────────────────��
         │
         ��
��────────────────────────��
│  Create Transaction    │
│  (PENDING)             │
��────────────────────────��
         │
         ��
��────────────────────────��
│  Initiate Transfer     │
│  (Provider Adapter)    │
��────────────────────────��
         │
         ��
��────────────────────────��
│  Update Vault/Goal     │
│  Send Notification     │
│  Audit Log             │
��────────────────────────��
```

## Data Integrity & Financial Safety

### Idempotency
- Every financial operation requires unique `idempotency_key`
- Database unique constraint prevents duplicates
- Client generates UUID per operation

### Optimistic Locking
- All entities have `@Version` field
- Concurrent updates fail with `OptimisticLockException`
- Client retries with fresh data

### Transaction State Machine
```
PENDING → PROCESSING → SUCCESS
                │
                ├──→ FAILED → (retry up to 3x)
                │
                └──→ REVERSED (compensation)
```

### Audit Trail
- All mutations logged to `audit_logs`
- Includes: actor, action, resource, old/new values, correlation ID
- Immutable, append-only

## Security Architecture

### Authentication
```
��─────────────��     ��─────────────��     ��─────────────��
│   Client    │────��│  API Gateway│────��│  Auth Svc   │
│  (Flutter)  │     │  (Spring)   │     │  (JWT)      │
��─────────────��     └─────────────��     └─────────────��
                           │
                    ��──────��──────��
                    ��             ��
              Access Token   Refresh Token
              (15 min)       (7 days, rotatable)
```

### Authorization (RBAC)
- **Roles**: SUPER_ADMIN, ADMIN, SUPPORT, COMPLIANCE, USER
- **Permissions**: Resource-action based (e.g., `user:read`, `savings:rule:write`)
- **Method-level**: `@PreAuthorize("hasPermission('savings', 'write')")`

### Data Protection
- **At Rest**: AES-256 for PII, BCrypt for passwords
- **In Transit**: TLS 1.3 enforced
- **Secrets**: HashiCorp Vault / AWS Secrets Manager (prod)
- **Logging**: Structured, no sensitive data

## Scalability Patterns

### Horizontal Scaling
- Stateless services (backend, admin)
- Redis for session/cache sharing
- Database read replicas for queries

### Background Processing
- `@Async` for notifications, emails
- Scheduled jobs for reconciliation
- Webhook processing with retry queue

### Caching Strategy
- Redis: Sessions, OTP, rate limits, user preferences
- Caffeine: Provider configs, reference data
- HTTP: ETag, Cache-Control headers

## Monitoring & Observability

### Metrics (Prometheus)
- HTTP latency, error rates
- Savings engine throughput
- Provider API health
- Database connection pool

### Logging
- Structured JSON (Logstash format)
- Correlation IDs for request tracing
- Security events separate stream

### Alerting
- Failed transactions > threshold
- Provider downtime
- Authentication anomalies
- Disk/memory/CPU thresholds

## Database Design

### Key Tables
| Table | Purpose | Key Indexes |
|-------|---------|-------------|
| users | Core user data | email, phone, kyc_status |
| financial_accounts | Linked accounts | user_id, provider_id, is_primary |
| savings_vaults | Savings containers | user_id, status, unlock_date |
| savings_rules | Automation rules | user_id, vault_id, is_active |
| savings_goals | Target-based savings | user_id, vault_id, status |
| transactions | All financial movements | user_id, type, status, created_at |
| income_transactions | Qualifying income | user_id, account_id, processed |
| savings_transactions | Executed savings | user_id, vault_id, rule_id |
| audit_logs | Immutable audit trail | user_id, resource, created_at |
| security_events | Threat detection | user_id, type, severity, resolved |

### Migrations
- Flyway for versioned migrations
- `V1__initial_schema.sql` - Core schema
- `V2__seed_data.sql` - Reference data
- Naming: `V{version}__{description}.sql`

## Deployment Architecture

```
                    ��─────────────��
                    │   CloudFlare │
                    │   (WAF, CDN) │
                    └──────��──────��
                           │
                    ��──────��──────��
                    │  Load Balancer│
                    │   (ALB/NLB)   │
                    └──────��──────��
                           │
          ��────────────────��────────────────��
          ��                ��                ��
    ��──────────��     ��──────────��     ��──────────��
    │ Backend 1│     │ Backend 2│     │ Backend N│
    │ (Spring) │     │ (Spring) │     │ (Spring) │
    └────��─────��     └────��─────��     └────��─────��
         │                │                │
         └────────────────��────────────────��
                          ��
              ��───────────────────────��
              │   PostgreSQL Primary   │
              │   + Read Replicas      │
              └───────────────────────��
                          │
         ��────────────────��────────────────��
         ��                ��                ��
    ��─────────��     ��─────────��     ��─────────��
    │ Redis   │     │ Kafka   │     │ S3/MinIO│
    │ Cluster │     │ (Events)│     │ (Files) │
    └─────────��     └─────────��     └─────────��
```

## Future Extensibility

### Adding New Provider
1. Implement `FinancialProvider` interface
2. Register in `ProviderRegistry`
3. Add provider entity via migration
4. Configure in admin dashboard

### Adding New Savings Rule
1. Extend `SavingsRuleType` enum
2. Add calculation logic in `SavingsRule.calculateSavingsAmount()`
3. Update validation in `SavingsRuleService`
4. Add UI support in mobile/admin

### Multi-Currency Support
- Currency field on all monetary entities
- Exchange rate service integration
- Display formatting per locale

### Internationalization
- i18n keys in backend messages
- Flutter `intl` package for mobile
- RTL support in admin dashboard