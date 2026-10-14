# Bikangu Security Guide

## Security Principles

1. **Defense in Depth** - Multiple layers of security controls
2. **Least Privilege** - Minimum permissions required
3. **Zero Trust** - Verify everything, trust nothing
4. **Secure by Default** - Secure configurations out of the box
5. **Audit Everything** - Immutable audit trail for all operations
6. **Fail Secure** - Errors default to secure state

---

## Authentication Security

### Password Policy
- Minimum 8 characters
- Requires: uppercase, lowercase, number, special character
- BCrypt with cost factor 12 (~250ms per hash)
- Breached password detection (HaveIBeenPwned API)
- Maximum 5 failed attempts before lockout (15 minutes)

### JWT Token Security
```java
// Access Token (15 minutes)
{
  "iss": "bikangu",
  "sub": "user-uuid",
  "email": "user@example.com",
  "roles": ["USER"],
  "permissions": ["savings:rule:write", ...],
  "type": "ACCESS",
  "iat": 1705312800,
  "exp": 1705313700
}

// Refresh Token (7 days, rotatable)
{
  "iss": "bikangu",
  "sub": "user-uuid",
  "type": "REFRESH",
  "iat": 1705312800,
  "exp": 1705898400
}
```

**Token Storage:**
- Mobile: Flutter Secure Storage (Keychain/Keystore)
- Web: HttpOnly, Secure, SameSite=Strict cookies
- Never in localStorage or sessionStorage

**Refresh Token Rotation:**
1. Client sends refresh token
2. Server validates, revokes old token
3. Issues new access + refresh token pair
4. Old refresh token marked as `replaced_by_token_id`
5. Concurrent use detection → revoke all user tokens

### PIN Security
- 6-digit numeric PIN
- Stored as SHA-256 hash in secure storage
- Biometric unlock decrypts PIN hash for verification
- Maximum 3 failed attempts before requiring password
- Separate from account password

### Biometric Authentication
- **Local only**: Biometrics unlock locally stored credentials
- **No server biometrics**: Server never receives biometric data
- **Fallback**: PIN/password always available
- **Re-authentication**: Required for sensitive operations (withdrawal, settings)

### Multi-Factor Authentication
- Email verification (registration, password reset)
- Phone/OTP verification (login from new device, withdrawal)
- Device fingerprinting (device ID, OS, browser)
- New device alerts via email/push

---

## Authorization Security

### Role-Based Access Control (RBAC)

| Role | Description | Permissions |
|------|-------------|-------------|
| SUPER_ADMIN | Full system access | All permissions |
| ADMIN | Management access | User mgmt, accounts, transactions, audit |
| SUPPORT | Customer support | Read users, accounts, suspend users |
| COMPLIANCE | KYC/AML review | User KYC, transactions, audit logs |
| USER | Regular user | Own data only |

### Permission Model
```
Resource:Action
├── user:read, write, delete, list, suspend, kyc
├── account:read, write, verify
├── savings:rule:read, write, delete
├── savings:vault:read, write, withdraw
├── savings:goal:read, write
├── transaction:read, export
├── provider:read, write, webhook
├── analytics:read, export
├── notification:read, write, preferences
��── admin:dashboard, settings, audit, security
```

### Method-Level Security
```java
@PreAuthorize("hasPermission('savings', 'write')")
public SavingsRule createRule(...)

@PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#userId, authentication)")
public User getUser(UUID userId)
```

---

## Data Protection

### Encryption at Rest
| Data Type | Encryption |
|-----------|------------|
| Passwords | BCrypt (cost 12) |
| PINs | SHA-256 + Secure Storage |
| Access Tokens | AES-256-GCM (in secure storage) |
| Refresh Tokens | SHA-256 hash in DB, encrypted in storage |
| PII (ID docs) | AES-256-GCM |
| Database | Transparent Data Encryption (TDE) |
| Backups | AES-256 encrypted |

### Encryption in Transit
- **TLS 1.3** enforced everywhere
- **Certificate Pinning** on mobile app
- **HSTS** headers on all responses
- **mTLS** for service-to-service (production)

### Key Management
```
Production:
├── HashiCorp Vault / AWS KMS
├── Automatic rotation (90 days)
├── Separate keys per environment
├── Audit logging on key access
��── Emergency revocation procedure

Development:
├── .env files (gitignored)
├── Pre-generated keys in .env.example
��── Rotate manually
```

### Data Classification
| Classification | Examples | Handling |
|----------------|----------|----------|
| **Public** | Marketing content, public APIs | No encryption required |
| **Internal** | System configs, logs | TLS in transit |
| **Confidential** | User profiles, transactions | AES-256 at rest, TLS |
| **Restricted** | Passwords, PINs, keys, ID docs | AES-256 + HSM, strict access |

---

## Financial Transaction Security

### Idempotency Protection
```java
// Every financial operation requires unique key
@UniqueConstraint(columnNames = "idempotency_key")
@Entity
class Transaction {
    String idempotencyKey; // UUID v4 per operation
}
```

**Client Implementation:**
```dart
// Generate once per user action
final idempotencyKey = Uuid().v4();
// Retry with SAME key
await api.initiateTransfer(request.copyWith(idempotencyKey: idempotencyKey));
```

### Transaction State Machine
```
PENDING → PROCESSING → SUCCESS
                │
                ├────→ FAILED (max 3 retries)
                │           │
                │           └────→ MANUAL_REVIEW
                │
                └────→ REVERSED (compensation)
```

### Balance Integrity
- **Optimistic Locking**: `@Version` on all financial entities
- **Atomic Operations**: Database transactions for multi-table updates
- **Reconciliation**: Daily job compares ledger vs provider balances
- **Immutable Ledger**: Transactions never deleted, only reversed

### Fraud Detection
| Signal | Action |
|--------|--------|
| New device + large transaction | Require OTP + email confirmation |
| Velocity (5+ txns/minute) | Block, alert security team |
| Geo-impossible login | Lock account, require verification |
| Round-number transfers | Additional review |
| New beneficiary + large amount | 24-hour delay + confirmations |

---

## API Security

### Rate Limiting
```yaml
# Per endpoint limits
auth:
  register: 5/min/IP
  login: 10/min/IP
  refresh: 20/min/user
api:
  default: 100/min/user
  financial: 30/min/user
webhooks:
  provider: 60/min/provider
admin:
  default: 200/min/admin
```

### Input Validation
- **Bean Validation** on all DTOs
- **Whitelist** allowed values for enums
- **Sanitization** for text fields (XSS prevention)
- **Size limits** on all payloads (10MB max)

### Security Headers
```http
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=()
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
X-XSS-Protection: 1; mode=block
```

### CORS Policy
```java
// Production: Specific origins only
allowedOrigins: ["https://app.bikangu.com", "https://admin.bikangu.com"]
allowedMethods: [GET, POST, PUT, PATCH, DELETE]
allowedHeaders: [Authorization, Content-Type, X-Device-Id]
allowCredentials: true
maxAge: 3600
```

---

## Mobile App Security

### Code Protection
- **R8/ProGuard** minification + obfuscation
- **Flutter** `--obfuscate --split-debug-info`
- **Certificate Pinning** for API endpoints
- **Root/Jailbreak Detection** (warn, don't block)

### Secure Storage
```dart
// Flutter Secure Storage
// Android: EncryptedSharedPreferences (AES-256)
// iOS: Keychain (kSecAttrAccessibleFirstUnlockThisDevice)

await secureStorage.write(key: 'access_token', value: token);
await secureStorage.write(key: 'pin_hash', value: hashPin(pin));
```

### Network Security
```xml
<!-- network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.bikangu.com</domain>
        <pin-set expiration="2025-01-01">
            <pin algorithm="SHA-256">base64-encoded-pin</pin>
            <pin algorithm="SHA-256">backup-pin</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

### Runtime Protection
- **Anti-tampering**: Checksum verification on startup
- **Debug Detection**: Disable features in debug builds
- **Screenshot Prevention**: FLAG_SECURE on sensitive screens
- **Clipboard Protection**: Disable copy on PIN/OTP fields

---

## Infrastructure Security

### Network Architecture
```
Internet
    │
    ��
��─────────────��
│ CloudFlare  │ ── WAF, DDoS, Bot Protection
│ (CDN/WAF)   │
��──────��──────��
       │
    ��──��──��
    │ ALB │ ── TLS Termination, Health Checks
    └──��──��
       │
��──────��──────��──────────────��
│  Backend    │   Database   │
│  (Private)  │  (Private)   │
│  Subnet     │  Subnet      │
��─────────────��──────────────��
       │
    ��──��──��
    │ NAT │ ── Outbound (provider APIs, FCM)
    └─────��
```

### Container Security
```dockerfile
# Non-root user
RUN addgroup -g 1000 -S bikangu && \
    adduser -u 1000 -S bikangu -G bikangu
USER bikangu

# Read-only filesystem
# readOnlyRootFilesystem: true (K8s)

# Drop capabilities
# securityContext:
#   capabilities:
#     drop: ["ALL"]
```

### Secrets Management
```yaml
# Kubernetes Secrets (SealedSecrets/ExternalSecrets)
apiVersion: v1
kind: Secret
metadata:
  name: bikangu-secrets
type: Opaque
stringData:
  JWT_SECRET: ${JWT_SECRET}
  DB_PASSWORD: ${DB_PASSWORD}
  REDIS_PASSWORD: ${REDIS_PASSWORD}
  MAIL_PASSWORD: ${MAIL_PASSWORD}
  FCM_SERVER_KEY: ${FCM_SERVER_KEY}
```

### Database Security
- **Parameterized Queries** only (JPA/Hibernate)
- **Row Level Security** for multi-tenancy (future)
- **Audit Triggers** on sensitive tables
- **Connection Pool** with validation queries
- **SSL Mode**: `verify-full` for connections

---

## Monitoring & Incident Response

### Security Event Types
| Type | Severity | Auto-Action |
|------|----------|-------------|
| BRUTE_FORCE_ATTEMPT | HIGH | Lock account, alert |
| SUSPICIOUS_LOGIN | HIGH | Require MFA, alert |
| MULTIPLE_FAILED_LOGINS | MEDIUM | Rate limit, log |
| ACCOUNT_LOCKED | HIGH | Notify user, audit |
| PRIVILEGE_ESCALATION_ATTEMPT | CRITICAL | Block IP, alert team |
| LARGE_TRANSACTION | MEDIUM | Additional verification |
| RAPID_TRANSACTIONS | HIGH | Velocity check, block |

### Alerting Rules
```promql
# Failed login rate
rate(auth_failed_login_total[5m]) > 0.1

# Transaction failure rate
rate(savings_transaction_failed_total[5m]) > 0.05

# Provider downtime
up{job="financial-providers"} == 0

# Security events
increase(security_events_total{severity="HIGH"}[15m]) > 0
```

### Incident Response Playbook

#### 1. Account Compromise
1. Lock affected account(s)
2. Revoke all tokens
3. Force password reset
4. Review recent transactions
5. Notify user via email/SMS
6. Document in security incident log

#### 2. Provider API Compromise
1. Disable provider in registry
2. Block provider webhook endpoint
3. Review recent transactions via provider
4. Notify affected users
5. Coordinate with provider security team

#### 3. Data Breach
1. Activate incident response team
2. Contain affected systems
3. Assess scope (what data, how many users)
4. Notify regulators (72 hours GDPR)
5. Notify affected users
6. Post-incident review

---

## Compliance

### PCI DSS (SAQ A)
- No card data stored, processed, or transmitted
- Redirect to PCI-validated payment providers
- Annual self-assessment questionnaire

### GDPR / Data Protection
- **Lawful Basis**: Consent + Contract
- **Data Minimization**: Only collect required data
- **Right to Access**: `/api/v1/profile` + export
- **Right to Erasure**: Account deletion with data purge
- **Data Portability**: JSON/CSV export
- **DPO**: Designated Data Protection Officer
- **DPIA**: For new high-risk processing

### Financial Regulations (Rwanda)
- **NBR Regulations**: Compliance with National Bank of Rwanda
- **KYC/AML**: Tiered KYC, transaction monitoring
- **Data Localization**: Primary data in Rwanda
- **Audit Reports**: Quarterly to regulator

### SOC 2 Type II (Target)
- **Security**: Access controls, encryption, monitoring
- **Availability**: 99.9% uptime SLA
- **Processing Integrity**: Idempotency, reconciliation
- **Confidentiality**: Encryption, access controls
- **Privacy**: GDPR compliance, data minimization

---

## Secure Development Lifecycle

### Code Review Checklist
- [ ] No hardcoded secrets
- [ ] Input validation on all endpoints
- [ ] Authorization checks on all methods
- [ ] Parameterized queries only
- [ ] Error messages don't leak info
- [ ] Logging doesn't include PII/secrets
- [ ] Cryptography uses approved libraries
- [ ] Dependencies scanned for vulnerabilities

### Dependency Management
```gradle
// Dependency check in CI
dependencyCheck {
    suppressionFile = 'dependency-check-suppressions.xml'
    failBuildOnCVSS = 7
}

// Automated updates
dependabot:
  - package-ecosystem: "gradle"
    directory: "/backend"
    schedule:
      interval: "weekly"
  - package-ecosystem: "pub"
    directory: "/mobile"
    schedule:
      interval: "weekly"
```

### Security Testing
| Test | Frequency | Tool |
|------|-----------|------|
| SAST | Every PR | SonarQube, SpotBugs |
| DAST | Weekly | OWASP ZAP |
| SCA | Every PR | OWASP Dependency Check |
| Penetration Test | Quarterly | External firm |
| Secret Scanning | Every Commit | GitLeaks, TruffleHog |

---

## Security Contacts

- **Security Team**: security@bikangu.com
- **Bug Bounty**: https://bikangu.com/bug-bounty
- **Incident Reporting**: incidents@bikangu.com
- **PGP Key**: Available on key servers (fingerprint: ABCD 1234...)

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024-01-15 | Initial security guide |