# Bikangu — Production-Ready Mobile FinTech Savings Platform

Bikangu is a smart savings platform that helps people automatically save money whenever they receive income. Users configure savings rules (percentage or fixed amount), and the system automatically moves money from connected financial accounts into secure savings vaults when qualifying income transactions are detected.

## ��� Architecture Overview

```
bikangu/
├── mobile/                 # Flutter cross-platform mobile app
├── backend/                # Spring Boot REST API
├── admin/                  # React + TypeScript admin dashboard
├── infrastructure/         # Docker, database scripts, mock providers
��── docs/                   # Documentation
```

### Technology Stack

**Mobile (Flutter)**
- Flutter 3.16+, Dart 3.2+
- Riverpod (state management)
- GoRouter (navigation)
- Dio + Retrofit (networking)
- Freezed + JSON Serializable (serialization)
- Flutter Secure Storage (secure storage)
- Firebase Cloud Messaging (push notifications)
- Material 3 + Custom Theme

**Backend (Spring Boot 3.2)**
- Java 17
- Spring Boot 3.2, Spring Security, Spring Data JPA
- PostgreSQL 16 + Flyway migrations
- Redis 7 (caching, sessions, OTP)
- JWT (access/refresh tokens)
- BCrypt (password hashing)
- Bean Validation
- Micrometer + Prometheus (metrics)

**Admin Dashboard (React)**
- React 18 + TypeScript + Vite
- TanStack Query (server state)
- React Router 6
- Tailwind CSS
- Recharts (visualizations)
- Zustand (client state)

**Infrastructure**
- Docker + Docker Compose
- PostgreSQL, Redis
- Nginx (reverse proxy for admin)

---

## ��� Quick Start

### Prerequisites
- Docker 24+ and Docker Compose 2.20+
- OR: Java 17, PostgreSQL 16, Redis 7, Node.js 20, Flutter 3.16+

### Using Docker (Recommended)

```bash
# Clone and navigate
cd bikangu

# Copy environment file
cp .env.example .env

# Edit .env with your configuration
# At minimum, set JWT_SECRET and email credentials

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop services
docker-compose down
```

### Services URLs
- **Backend API**: http://localhost:8080/api/v1
- **Admin Dashboard**: http://localhost:3000
- **Mock Provider**: http://localhost:8081
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **Actuator Health**: http://localhost:8080/actuator/health

---

## ��� Mobile App Development

```bash
cd mobile

# Install dependencies
flutter pub get

# Generate code (run after model changes)
flutter pub run build_runner build --delete-conflicting-outputs

# Run on device/emulator
flutter run

# Run tests
flutter test

# Build for release
flutter build apk --release
flutter build ios --release
```

### Project Structure (Clean Architecture)
```
lib/
├── core/
│   ├── constants/       # App constants
│   ├── error/           # Exceptions & failures
│   ├── network/         # API client, interceptors
│   ├── providers/       # Riverpod providers
│   ├── storage/         # Secure storage, preferences
│   └── utils/           # Formatters, validators
├── config/              # App config, theme, routing
├── routing/             # GoRouter configuration
├── theme/               # Material 3 themes
├── shared/
│   ├── models/          # Freezed data models
│   ├── widgets/         # Reusable UI components
│   └── extensions/      # Dart extensions
��── features/
    ├── authentication/  # Auth screens, providers
    ├── dashboard/       # Main dashboard
    ├── accounts/        # Financial accounts
    ├── savings/         # Vaults & rules
    ├── goals/           # Savings goals
    ├── transactions/    # Transaction history
    ├── analytics/       # Charts & insights
    ├── notifications/   # Notification center
    └── profile/         # User profile & settings
```

---

## ������ Backend Development

```bash
cd backend

# Run with Gradle
./gradlew bootRun

# Run tests
./gradlew test

# Build JAR
./gradlew bootJar

# Database migrations (Flyway)
./gradlew flywayMigrate
./gradlew flywayClean flywayMigrate  # Reset DB

# Check code quality
./gradlew check
```

### Project Structure (Modular)
```
src/main/java/com/bikangu/
├── authentication/      # Auth, JWT, sessions, OTP
├── users/               # User management, KYC
├── accounts/            # Financial accounts
├── providers/           # Provider abstraction + mock
├── transactions/        # Transactions, income detection
├── savings/             # Vaults, rules, savings engine
├── goals/               # Savings goals
├── notifications/       # FCM, email, in-app
├── analytics/           # Aggregations, dashboards
├── admin/               # Admin APIs
├── audit/               # Audit logging
├── security/            # Security events, monitoring
├── common/              # Shared domain, exceptions, utils
��── config/              # Spring configuration
```

### Key Features Implemented

**Authentication**
- Registration with email/phone verification
- JWT access tokens (15min) + refresh tokens (7 days)
- Refresh token rotation
- PIN authentication (6-digit)
- Biometric authentication support
- Account lockout after failed attempts
- Password reset flow

**Financial Accounts**
- Multi-provider support (MTN, Airtel, Banks, Mock)
- Provider abstraction layer
- Account verification
- Primary account selection
- Balance synchronization

**Savings Engine**
- Multiple rule types: Percentage, Fixed, Conditional, Income Source, Bonus
- Priority-based rule execution
- Idempotency protection
- Automatic vault/goal updates
- Transaction receipts

**Safety & Reliability**
- Optimistic locking
- Unique constraints on idempotency keys
- Transaction state machine
- Audit logging for all operations
- Security event monitoring

---

## ��� Mock Financial Provider

For development without real financial APIs, a mock provider simulates:

```bash
# Simulate salary received
curl -X POST "http://localhost:8080/api/v1/webhooks/mock/simulate-salary?accountId={id}&amount=600000"

# Simulate freelance payment
curl -X POST "http://localhost:8080/api/v1/webhooks/mock/simulate-freelance?accountId={id}&amount=150000&client=ClientName"

# Simulate bonus
curl -X POST "http://localhost:8080/api/v1/webhooks/mock/simulate-bonus?accountId={id}&amount=100000&source=YearEnd"
```

The mock provider supports test scenarios:
- `SUCCESS` - Normal transfer
- `INSUFFICIENT_FUNDS` - Balance too low
- `PROVIDER_TIMEOUT` - Simulated timeout
- `TRANSFER_FAILED` - Provider error

---

## ��� Security Considerations

- **Never commit secrets** - Use `.env` files
- **JWT secrets** - Minimum 256 bits, rotate regularly
- **Password hashing** - BCrypt cost 12
- **PIN storage** - Hashed in secure storage
- **Biometrics** - Local authentication only, unlocks stored credentials
- **Rate limiting** - Per-user and per-IP
- **Audit logging** - All financial operations
- **PCI compliance** - No card data stored
- **Encryption** - TLS 1.3, AES-256 for sensitive data at rest

---

## ��� API Documentation

### Authentication
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
POST   /api/v1/auth/verify-email
POST   /api/v1/auth/verify-phone
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
POST   /api/v1/auth/change-password
POST   /api/v1/auth/set-pin
```

### Financial Accounts
```
GET    /api/v1/accounts
GET    /api/v1/accounts/primary
POST   /api/v1/accounts
PATCH  /api/v1/accounts/{id}
DELETE /api/v1/accounts/{id}
POST   /api/v1/accounts/{id}/sync
GET    /api/v1/accounts/providers
```

### Savings
```
GET    /api/v1/savings/vaults
POST   /api/v1/savings/vaults
PATCH  /api/v1/savings/vaults/{id}
DELETE /api/v1/savings/vaults/{id}
GET    /api/v1/savings/rules
POST   /api/v1/savings/rules
PATCH  /api/v1/savings/rules/{id}
POST   /api/v1/savings/rules/{id}/pause
POST   /api/v1/savings/rules/{id}/resume
```

### Goals
```
GET    /api/v1/goals
POST   /api/v1/goals
PATCH  /api/v1/goals/{id}
DELETE /api/v1/goals/{id}
```

### Transactions
```
GET    /api/v1/transactions
GET    /api/v1/transactions/{id}
GET    /api/v1/transactions/income
GET    /api/v1/transactions/savings
```

### Analytics
```
GET    /api/v1/analytics/dashboard
GET    /api/v1/analytics?months=12
```

### Admin (Requires ADMIN role)
```
GET    /api/v1/admin/users
GET    /api/v1/admin/audit-logs
GET    /api/v1/admin/security-events
GET    /api/v1/admin/providers
```

Full OpenAPI/Swagger docs available at: http://localhost:8080/swagger-ui.html

---

## ��� Testing

### Backend Tests
```bash
cd backend
./gradlew test                    # Unit tests
./gradlew integrationTest         # Integration tests (requires DB)
```

### Mobile Tests
```bash
cd mobile
flutter test                      # Unit & widget tests
flutter drive --target=test_driver/app.dart  # Integration tests
```

### Test Scenarios Covered
- Salary received → savings calculated → transfer executed
- Multiple rules with priorities
- Insufficient funds handling
- Duplicate webhook prevention (idempotency)
- Failed transfer retry logic
- Locked vault withdrawal restrictions
- Goal completion detection
- Authentication flows
- Authorization (RBAC)

---

## ��� Deployment

### Production Checklist
- [ ] Set strong `JWT_SECRET` (256+ bits)
- [ ] Configure production PostgreSQL with SSL
- [ ] Set up Redis cluster with persistence
- [ ] Configure SMTP with valid credentials
- [ ] Set up Firebase project for FCM
- [ ] Configure real financial provider credentials
- [ ] Set up SSL/TLS certificates
- [ ] Configure monitoring (Prometheus + Grafana)
- [ ] Set up log aggregation (ELK/Loki)
- [ ] Configure backup strategy for PostgreSQL
- [ ] Run security scanning (OWASP ZAP, Snyk)
- [ ] Load test savings engine

### Docker Production
```bash
# Build production images
docker-compose -f docker-compose.yml -f docker-compose.prod.yml build

# Deploy
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Kubernetes (Helm charts in `infrastructure/helm`)

---

## ��� Documentation

- [Architecture](docs/architecture.md)
- [API Reference](docs/api.md)
- [Security Guide](docs/security.md)
- [Provider Integration](docs/provider-integration.md)
- [Deployment Guide](docs/deployment.md)

---

## ��� Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

### Code Standards
- Follow existing code style
- Write tests for new features
- Update documentation
- No hardcoded secrets
- Conventional commits

---

## ��� License

Proprietary - All rights reserved.

---

## ��� Support

For questions or support:
- Create an issue in the repository
- Email: support@bikangu.com
- Documentation: https://docs.bikangu.com