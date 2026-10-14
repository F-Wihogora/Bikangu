# Bikangu Mobile App - Feature Screens

This document outlines the key screens for each feature in the Flutter mobile app.

---

## Authentication Flow

### SplashScreen
- App logo animation
- Check auth state
- Navigate to appropriate screen

### OnboardingScreen
- 4-page carousel:
  1. "Welcome to Bikangu" - Auto-save when you earn
  2. "Smart Savings Rules" - Percentage or fixed amount
  3. "Secure Vaults" - Locked savings with goals
  4. "Track Progress" - Analytics & insights
- "Get Started" button → Register/Login

### RegisterScreen
- Email, phone (optional), password, confirm password
- First name, last name
- Date of birth, gender, nationality
- Terms & conditions checkbox
- "Create Account" → VerifyEmailScreen

### LoginScreen
- Email, password
- "Forgot Password?" link
- "Don't have an account? Register" link
- Biometric login option (if enabled)
- Remember me checkbox

### VerifyEmailScreen
- "Check your email" message
- Resend email button (cooldown timer)
- "I've verified, continue" button
- Skip for now (with warning)

### VerifyPhoneScreen
- Phone number input (pre-filled from registration)
- 6-digit OTP input (auto-focus, auto-submit)
- Resend OTP (cooldown)
- "Verify" → CreatePinScreen

### CreatePinScreen
- "Create your 6-digit PIN"
- PIN input (6 circles, auto-advance)
- Confirm PIN
- "Enable biometrics?" toggle
- "Continue" → BiometricsScreen

### BiometricsScreen
- "Unlock with Face ID / Fingerprint"
- Enable/Disable toggle
- "Skip for now" option
- "Continue" → DashboardScreen

---

## Dashboard

### DashboardScreen (Main)
**Bottom Navigation**: Home | Savings | Transactions | Analytics | Profile

**Content**:
- **Balance Cards Row**: Total Savings, This Month, Available
- **Quick Actions**: Simulate Salary, Create Goal, Link Account
- **Active Goals**: Horizontal scroll of goal progress cards
- **Recent Transactions**: Last 5 transactions with type icons
- **Upcoming Savings**: Next scheduled deductions
- **Pull-to-refresh** for sync

### DashboardSummary Widget
```dart
// Real-time data from /analytics/dashboard
- Total Savings (RWF)
- This Month Savings
- Locked Savings
- Available Balance
- Active Goals Count
- Total Vaults Count
```

---

## Accounts

### AccountsScreen
- **Primary Account Card**: Large, prominent
- **Linked Accounts List**: Cards with provider logo, masked number, balance
- **Add Account Button** → LinkAccountScreen
- **Account Actions**: Set Primary, Sync Balance, Refresh, Unlink

### LinkAccountScreen
1. **Select Provider**: Grid of provider cards (logo, name, category)
2. **Enter Credentials**: Account ID (phone/account number), PIN/password
3. **Account Name**: Custom name (optional)
4. **Set as Primary**: Toggle
5. **Verify** → Loading → Success/Error

---

## Savings

### SavingsScreen
**Tabs**: Vaults | Rules

#### Vaults Tab
- **Vault Cards**: Progress ring, name, current/target, lock status
- **Create Vault FAB** → CreateVaultScreen
- **Vault Actions**: Deposit, Withdraw (if unlocked), Edit, Close

#### Rules Tab
- **Rule Cards**: Name, vault, type, percentage/amount, status
- **Create Rule FAB** → CreateRuleScreen
- **Rule Actions**: Pause/Resume, Edit, Delete

### CreateVaultScreen
- **Basic Info**: Name, description, icon picker, color picker
- **Target**: Target amount, currency
- **Lock Settings**: Lock date, unlock date, auto-lock toggle
- **Advanced**: Interest rate, early withdrawal, penalty, minimum balance
- **Preview**: Shows progress calculation

### CreateRuleScreen
- **Select Vault**: Dropdown of user's vaults
- **Rule Type**: Radio buttons (Percentage, Fixed, Conditional, Income Source, Bonus)
- **Configuration**:
  - Percentage: Slider 1-100%
  - Fixed: Amount input
  - Conditional: Percentage + min/max thresholds
  - Income Source: Percentage + source selection
  - Bonus: Percentage
- **Priority**: Number input
- **Save** → Returns to SavingsScreen

### VaultDetailScreen
- **Header**: Large progress ring, vault name, lock status badge
- **Stats**: Current, Target, Remaining, Progress %
- **Timeline**: Lock date, unlock date, days remaining
- **Transactions**: List of savings deposits to this vault
- **Actions**: Deposit, Withdraw, Edit, Close

---

## Goals

### GoalsScreen
- **Goal Cards**: Progress bar, name, current/target, deadline
- **Filter Chips**: All | Active | Completed | Overdue
- **Create Goal FAB** → CreateGoalScreen

### CreateGoalScreen
- **Link to Vault**: Optional dropdown (creates vault if none)
- **Basic Info**: Name, description, icon, color
- **Target**: Amount, currency
- **Timeline**: Deadline, lock date
- **Auto-contribute**: Toggle, frequency, amount
- **Priority**: Number

### GoalDetailScreen
- **Progress**: Large progress bar with percentage
- **Contribution History**: Timeline of deposits
- **Projected Completion**: Based on current rate
- **Actions**: Contribute now, Edit, Delete

---

## Transactions

### TransactionsScreen
- **Filter Bar**: Search, Filter (type, date range, category, goal)
- **Tabs**: All | Income | Savings | Withdrawals
- **List**: Grouped by date, infinite scroll
- **Transaction Row**: Icon, description, amount (color-coded), date, status
- **Pull-to-refresh**, Empty state

### TransactionDetailScreen
- **Header**: Type badge, status badge, amount
- **Details**: Reference, date, provider, account, category
- **Related**: Vault, Goal, Rule (if applicable)
- **Actions**: Download receipt, Share, Report issue

---

## Analytics

### AnalyticsScreen
**Time Range Selector**: 1M | 3M | 6M | 1Y | All

**Charts**:
1. **Savings Growth**: Area chart (monthly savings)
2. **Income vs Savings**: Grouped bar chart
3. **Savings by Category**: Pie chart
4. **Savings Rate**: Line chart (% of income saved)

**Summary Cards**:
- Total Saved, Avg Monthly, Best Month, Savings Rate

**Export**: PDF report, CSV data

---

## Notifications

### NotificationsScreen
- **Tabs**: All | Unread
- **List**: Grouped by date, swipe actions (mark read, delete)
- **Notification Card**: Icon, title, message, time, action button
- **Mark All Read** button
- **Settings Link** → NotificationPreferencesScreen

### NotificationPreferencesScreen
- **Channels**: Email, Push, In-App, SMS toggles
- **Categories**: Toggle per notification type
- **Quiet Hours**: Start/End time picker
- **Timezone**: Selector

---

## Profile

### ProfileScreen
- **Header**: Avatar, name, email, phone, KYC status badge
- **Menu Sections**:
  - **Account**: Personal Info, Security, Notifications
  - **Preferences**: Theme, Language, Currency
  - **Support**: Help, FAQ, Contact, Terms, Privacy
  - **Actions**: Logout, Delete Account

### SecurityScreen
- **PIN**: Change PIN (current + new + confirm)
- **Biometrics**: Toggle with auth
- **Sessions**: Active devices list, "Logout all"
- **Login History**: Recent logins with location/device

### NotificationPreferencesScreen
(See Notifications section)

---

## Key UI Components

### Custom Widgets
```dart
// Reusable across features
- BalanceCard          // Dashboard, Accounts
- StatCard             // Dashboard, Analytics
- GoalProgressCard     // Savings, Goals, Dashboard
- SavingsProgressChart // Analytics, VaultDetail
- TransactionTile      // Transactions, Dashboard
- ProviderCard         // Accounts, LinkAccount
- RuleCard             // Savings, Dashboard
- VaultCard            // Savings, Dashboard
- EmptyState           // All lists
- ErrorState           // All screens
- LoadingOverlay       // All async operations
- LoadingShimmer       // All lists/grids
- AppCard              // Base card
- AppCardWithHeader    // Sectioned cards
- PrimaryButton        // All actions
- SecondaryButton      // Secondary actions
- AppTextField         // All forms
- PinInputField        // PIN, OTP
- SearchField          // All searchable lists
```

### Theme
- **Light/Dark**: Full Material 3 support
- **Primary**: Teal (#006D77)
- **Secondary**: Orange (#E29578)
- **Typography**: Inter font family
- **Spacing**: 8dp base unit
- **Radius**: 12dp cards, 16dp buttons, 20dp sheets

---

## State Management (Riverpod)

### Providers by Feature
```dart
// Auth
authProvider          // AuthState (user, tokens, onboarding)
secureStorageProvider // SecureStorageService
preferencesProvider   // PreferencesService

// Accounts
accountsProvider      // AccountsState (accounts, providers, primary)

// Savings
savingsProvider       // SavingsState (vaults, rules)

// Goals
goalsProvider         // GoalsState (goals)

// Transactions
transactionsProvider  // Paginated transactions

// Analytics
analyticsProvider     // DashboardSummary, AnalyticsData

// Notifications
notificationsProvider // Notifications, unread count

// Profile
profileProvider       // UserProfile, preferences
```

### Data Flow
```
UI → Controller/Notifier → ApiService → Backend
                ↓
         SecureStorage (tokens)
                ↓
         State Update → UI Rebuild
```

---

## Navigation (GoRouter)

### Route Structure
```
/splash
/onboarding
/auth/login
/auth/register
/auth/verify-email
/auth/verify-phone
/auth/create-pin
/auth/biometrics

/dashboard (shell)
/savings (shell)
/savings/vault/create
/savings/vault/:vaultId
/savings/rule/create

/transactions (shell)
/transactions/:transactionId

/analytics (shell)

/profile (shell)
/profile/security
/profile/notifications

/accounts
/accounts/link

/goals
/goals/create
/goals/:goalId

/notifications
```

### Auth Guards
- Protected routes check `authProvider.isAuthenticated`
- Onboarding check `authProvider.isOnboardingComplete`
- Redirects handled in router `redirect` parameter

---

## API Integration

### Retrofit Service
```dart
@RestApi()
abstract class ApiService {
  // Auth
  @POST('/auth/register')
  Future<ApiResponse<AuthResponse>> register(@Body() RegisterRequest request);

  @POST('/auth/login')
  Future<ApiResponse<TokenResponse>> login(@Body() LoginRequest request);
  
  // ... all endpoints
}
```

### Error Handling
```dart
// Automatic via Dio interceptors
- 401 → Token refresh → Retry once
- 403 → Logout
- 409 (Idempotency) → Show "Already processed"
- 422 → Validation errors
- 5xx → Retry with exponential backoff
```

---

## Offline Support

### Caching Strategy
```dart
// Riverpod + Hive/SharedPreferences
- User profile: Cached indefinitely
- Vaults/Goals: Cached 1 hour
- Transactions: Cached 15 min
- Analytics: Cached 30 min
- Providers: Cached 24 hours

// Offline Queue
- Pending mutations stored locally
- Synced when online
- Conflict resolution: Server wins
```

---

## Testing Checklist

### Unit Tests
- [ ] Validators
- [ ] Formatters
- [ ] AuthNotifier logic
- [ ] SavingsEngine calculations
- [ ] Rule applicability logic

### Widget Tests
- [ ] PrimaryButton states
- [ ] AppTextField validation
- [ ] PinInputField behavior
- [ ] VaultCard rendering
- [ ] GoalProgressCard progress

### Integration Tests
- [ ] Register → Verify → PIN → Dashboard
- [ ] Link Account → Create Vault → Create Rule
- [ ] Simulate Salary → Savings Executed
- [ ] Create Goal → Track Progress
- [ ] Logout → Login → Biometrics

### Golden Tests
- [ ] Light theme screens
- [ ] Dark theme screens
- [ ] RTL layout (Arabic)
- [ ] Different screen sizes