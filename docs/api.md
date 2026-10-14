# Bikangu API Reference

## Base URL
- **Development**: `http://localhost:8080/api/v1`
- **Production**: `https://api.bikangu.com/api/v1`

## Authentication

All protected endpoints require a Bearer token:
```
Authorization: Bearer <access_token>
```

### Token Response
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900000
  }
}
```

---

## Authentication Endpoints

### Register
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "phoneNumber": "+250788123456",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "nationality": "Rwandan"
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Logout
```http
POST /auth/logout
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Verify Email
```http
POST /auth/verify-email
Content-Type: application/json

{
  "token": "verification-token-from-email"
}
```

### Verify Phone
```http
POST /auth/verify-phone
Content-Type: application/json

{
  "phoneNumber": "+250788123456",
  "otp": "123456"
}
```

### Forgot Password
```http
POST /auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### Reset Password
```http
POST /auth/reset-password
Content-Type: application/json

{
  "token": "reset-token-from-email",
  "newPassword": "NewSecurePass123!"
}
```

### Change Password
```http
POST /auth/change-password
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword"
}
```

### Set PIN
```http
POST /auth/set-pin
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "pin": "123456"
}
```

---

## Financial Accounts

### List Accounts
```http
GET /accounts
Authorization: Bearer <access_token>
```

### Get Primary Account
```http
GET /accounts/primary
Authorization: Bearer <access_token>
```

### Get Account Details
```http
GET /accounts/{accountId}
Authorization: Bearer <access_token>
```

### Link Account
```http
POST /accounts
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "providerId": "provider-uuid",
  "providerAccountId": "250788123456",
  "credentials": "pin_or_password",
  "accountName": "My MTN MoMo",
  "isPrimary": true
}
```

### Update Account
```http
PATCH /accounts/{accountId}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "accountName": "Updated Name",
  "isPrimary": true,
  "isActive": true
}
```

### Unlink Account
```http
DELETE /accounts/{accountId}
Authorization: Bearer <access_token>
```

### Sync Balance
```http
POST /accounts/{accountId}/sync
Authorization: Bearer <access_token>
```

### Refresh Account
```http
POST /accounts/{accountId}/refresh
Authorization: Bearer <access_token>
```

### List Providers
```http
GET /accounts/providers
Authorization: Bearer <access_token>
```

### List Providers by Country
```http
GET /accounts/providers/country/{countryCode}
Authorization: Bearer <access_token>
```

---

## Savings Vaults

### List Vaults
```http
GET /savings/vaults
Authorization: Bearer <access_token>
```

### List Active Vaults
```http
GET /savings/vaults/active
Authorization: Bearer <access_token>
```

### List Locked Vaults
```http
GET /savings/vaults/locked
Authorization: Bearer <access_token>
```

### Get Vault Details
```http
GET /savings/vaults/{vaultId}
Authorization: Bearer <access_token>
```

### Create Vault
```http
POST /savings/vaults
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "Emergency Fund",
  "description": "For unexpected expenses",
  "targetAmount": "1000000",
  "currency": "RWF",
  "icon": "shield",
  "color": "#006D77",
  "lockDate": "2025-12-31",
  "unlockDate": "2026-12-31",
  "interestRate": "0.05",
  "autoLock": true,
  "allowEarlyWithdrawal": false,
  "earlyWithdrawalPenaltyRate": "0.10",
  "minimumBalance": "10000"
}
```

### Update Vault
```http
PATCH /savings/vaults/{vaultId}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "Updated Name",
  "targetAmount": "1500000",
  "lockDate": "2026-06-30"
}
```

### Close Vault
```http
DELETE /savings/vaults/{vaultId}
Authorization: Bearer <access_token>
```

---

## Savings Rules

### List Rules
```http
GET /savings/rules
Authorization: Bearer <access_token>
```

### List Active Rules
```http
GET /savings/rules/active
Authorization: Bearer <access_token>
```

### Create Rule
```http
POST /savings/rules
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "vaultId": "vault-uuid",
  "name": "Save 20% of Salary",
  "description": "Automatic savings from salary",
  "ruleType": "PERCENTAGE",
  "percentage": "20.00",
  "minIncomeThreshold": "50000",
  "incomeSource": "SALARY",
  "priority": 1
}
```

### Update Rule
```http
PATCH /savings/rules/{ruleId}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "percentage": "25.00",
  "isActive": true
}
```

### Pause Rule
```http
POST /savings/rules/{ruleId}/pause
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "reason": "Temporary pause"
}
```

### Resume Rule
```http
POST /savings/rules/{ruleId}/resume
Authorization: Bearer <access_token>
```

### Delete Rule
```http
DELETE /savings/rules/{ruleId}
Authorization: Bearer <access_token>
```

---

## Savings Goals

### List Goals
```http
GET /goals
Authorization: Bearer <access_token>
```

### List Active Goals
```http
GET /goals/active
Authorization: Bearer <access_token>
```

### Get Upcoming Goals
```http
GET /goals/upcoming?days=30
Authorization: Bearer <access_token>
```

### Get Goal Details
```http
GET /goals/{goalId}
Authorization: Bearer <access_token>
```

### Create Goal
```http
POST /goals
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "vaultId": "vault-uuid",
  "name": "Vacation Fund",
  "description": "Summer vacation 2025",
  "targetAmount": "500000",
  "currency": "RWF",
  "icon": "plane",
  "color": "#E29578",
  "deadline": "2025-06-01",
  "priority": 1,
  "autoContribute": false
}
```

### Update Goal
```http
PATCH /goals/{goalId}
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "targetAmount": "600000",
  "deadline": "2025-07-01"
}
```

### Delete Goal
```http
DELETE /goals/{goalId}
Authorization: Bearer <access_token>
```

---

## Transactions

### List Transactions
```http
GET /transactions?page=0&size=20&type=SAVINGS&status=SUCCESS&startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z
Authorization: Bearer <access_token>
```

### Get Transaction Details
```http
GET /transactions/{transactionId}
Authorization: Bearer <access_token>
```

### List Income Transactions
```http
GET /transactions/income?page=0&size=20
Authorization: Bearer <access_token>
```

### List Savings Transactions
```http
GET /transactions/savings?page=0&size=20
Authorization: Bearer <access_token>
```

---

## Analytics

### Dashboard Summary
```http
GET /analytics/dashboard
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalSavings": "2500000",
    "thisMonthSavings": "150000",
    "lockedSavings": "2000000",
    "availableBalance": "500000",
    "activeGoalsCount": 3,
    "totalVaultsCount": 5,
    "recentTransactions": [...],
    "activeGoals": [...],
    "vaults": [...]
  }
}
```

### Analytics Data
```http
GET /analytics?months=12
Authorization: Bearer <access_token>
```

---

## Notifications

### List Notifications
```http
GET /notifications?page=0&size=20&unreadOnly=false
Authorization: Bearer <access_token>
```

### Get Unread Count
```http
GET /notifications/unread-count
Authorization: Bearer <access_token>
```

### Mark as Read
```http
PATCH /notifications/{notificationId}/read
Authorization: Bearer <access_token>
```

### Mark All as Read
```http
POST /notifications/read-all
Authorization: Bearer <access_token>
```

---

## Profile

### Get Profile
```http
GET /profile
Authorization: Bearer <access_token>
```

### Update Profile
```http
PATCH /profile
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "preferredLanguage": "en",
  "preferredCurrency": "RWF",
  "timezone": "Africa/Kigali"
}
```

### Change PIN
```http
POST /profile/change-pin
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "currentPin": "123456",
  "newPin": "654321"
}
```

### Toggle Biometrics
```http
POST /profile/biometrics
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "enabled": true
}
```

### Update Notification Preferences
```http
PATCH /profile/notifications
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "emailEnabled": true,
  "pushEnabled": true,
  "salaryReceived": true,
  "savingsExecuted": true,
  "securityAlerts": true
}
```

---

## Webhooks (Mock Provider)

### Simulate Income
```http
POST /webhooks/mock/simulate-income?accountId={id}&amount=100000&source=EMPLOYER&category=SALARY
Authorization: Bearer <access_token>
```

### Simulate Salary
```http
POST /webhooks/mock/simulate-salary?accountId={id}&amount=600000
Authorization: Bearer <access_token>
```

### Simulate Freelance
```http
POST /webhooks/mock/simulate-freelance?accountId={id}&amount=150000&client=AcmeCorp
Authorization: Bearer <access_token>
```

### Simulate Bonus
```http
POST /webhooks/mock/simulate-bonus?accountId={id}&amount=100000&source=YearEnd
Authorization: Bearer <access_token>
```

---

## Admin Endpoints

### Users
```http
GET /admin/users?page=0&size=20&search=john
Authorization: Bearer <admin_token>
```

### Audit Logs
```http
GET /admin/audit-logs?page=0&size=20&userId={id}&action=SAVINGS_RULE_CREATED
Authorization: Bearer <admin_token>
```

### Security Events
```http
GET /admin/security-events?page=0&size=20&severity=HIGH&resolved=false
Authorization: Bearer <admin_token>
```

### Providers
```http
GET /admin/providers
Authorization: Bearer <admin_token>
```

### System Settings
```http
GET /admin/settings
Authorization: Bearer <admin_token>

PATCH /admin/settings
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "maintenanceMode": false,
  "maxSavingsPercentage": 100,
  "defaultCurrency": "RWF"
}
```

---

## Error Responses

### Standard Error Format
```json
{
  "success": false,
  "data": null,
  "message": "Error description",
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-uuid",
    "pagination": null
  }
}
```

### Validation Error
```json
{
  "success": false,
  "data": null,
  "message": "Validation failed",
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-uuid",
    "details": {
      "email": "Email is required",
      "password": "Password must be at least 8 characters"
    }
  }
}
```

### Common HTTP Status Codes
| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request / Validation Error |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict / Idempotency Violation |
| 422 | Unprocessable Entity |
| 429 | Too Many Requests |
| 500 | Internal Server Error |
| 502 | Bad Gateway (Provider Error) |
| 503 | Service Unavailable |

---

## Rate Limiting

- **Authentication**: 10 requests/minute per IP
- **API**: 100 requests/minute per user
- **Webhooks**: 60 requests/minute per provider
- **Admin**: 200 requests/minute per admin

Headers returned:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1705312800
```

---

## Pagination

All list endpoints support pagination:
```
?page=0&size=20&sort=createdAt,desc
```

Response includes:
```json
{
  "content": [...],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## Webhooks (Production)

### Provider → Bikangu
```
POST /webhooks/{providerId}
Headers:
  X-Signature: <hmac-sha256>
  X-Event-Type: transaction.created
Content-Type: application/json

{
  "eventId": "evt-uuid",
  "eventType": "transaction.created",
  "timestamp": "2024-01-15T10:30:00Z",
  "payload": {
    "accountId": "provider-account-id",
    "transactionId": "txn-uuid",
    "amount": 600000,
    "currency": "RWF",
    "type": "CREDIT",
    "category": "SALARY",
    "description": "Salary payment",
    "timestamp": "2024-01-15T10:29:45Z"
  }
}
```

### Bikangu → Your System (Optional)
Configure webhook URLs in provider settings for:
- `savings.executed`
- `savings.failed`
- `goal.achieved`
- `withdrawal.requested`
- `account.verified`