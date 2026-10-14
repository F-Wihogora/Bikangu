import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';
import 'package:bikangu/shared/models/api_response.dart';
import 'package:bikangu/shared/models/auth_models.dart';
import 'package:bikangu/shared/models/account_models.dart';
import 'package:bikangu/shared/models/savings_models.dart';
import 'package:bikangu/shared/models/goal_models.dart';
import 'package:bikangu/shared/models/transaction_models.dart';
import 'package:bikangu/shared/models/analytics_models.dart';
import 'package:bikangu/shared/models/notification_models.dart';
import 'package:bikangu/shared/models/user_models.dart';
import 'package:bikangu/shared/models/provider_models.dart';

part 'api_service.g.dart';

@RestApi()
abstract class ApiService {
  factory ApiService(Dio dio, {String baseUrl}) = _ApiService;

  // Auth
  @POST('/auth/register')
  Future<ApiResponse<AuthResponse>> register(@Body() RegisterRequest request);

  @POST('/auth/login')
  Future<ApiResponse<TokenResponse>> login(@Body() LoginRequest request);

  @POST('/auth/refresh')
  Future<ApiResponse<TokenResponse>> refreshToken(@Body() RefreshTokenRequest request);

  @POST('/auth/logout')
  Future<ApiResponse<void>> logout(@Body() RefreshTokenRequest request);

  @POST('/auth/logout-all')
  Future<ApiResponse<void>> logoutAll();

  @POST('/auth/verify-email')
  Future<ApiResponse<void>> verifyEmail(@Body() VerifyEmailRequest request);

  @POST('/auth/verify-phone')
  Future<ApiResponse<void>> verifyPhone(@Body() VerifyPhoneRequest request);

  @POST('/auth/forgot-password')
  Future<ApiResponse<void>> forgotPassword(@Body() ForgotPasswordRequest request);

  @POST('/auth/reset-password')
  Future<ApiResponse<void>> resetPassword(@Body() ResetPasswordRequest request);

  @POST('/auth/change-password')
  Future<ApiResponse<void>> changePassword(@Body() ChangePasswordRequest request);

  @POST('/auth/set-pin')
  Future<ApiResponse<void>> setPin(@Body() SetPinRequest request);

  // Accounts
  @GET('/accounts')
  Future<ApiResponse<List<FinancialAccount>>> getAccounts();

  @GET('/accounts/primary')
  Future<ApiResponse<FinancialAccount?>> getPrimaryAccount();

  @GET('/accounts/{accountId}')
  Future<ApiResponse<FinancialAccount>> getAccount(@Path() String accountId);

  @POST('/accounts')
  Future<ApiResponse<FinancialAccount>> linkAccount(@Body() LinkAccountRequest request);

  @PATCH('/accounts/{accountId}')
  Future<ApiResponse<FinancialAccount>> updateAccount(@Path() String accountId, @Body() UpdateAccountRequest request);

  @DELETE('/accounts/{accountId}')
  Future<ApiResponse<void>> unlinkAccount(@Path() String accountId);

  @POST('/accounts/{accountId}/sync')
  Future<ApiResponse<FinancialAccount>> syncBalance(@Path() String accountId);

  @POST('/accounts/{accountId}/refresh')
  Future<ApiResponse<FinancialAccount>> refreshAccount(@Path() String accountId);

  @GET('/accounts/providers')
  Future<ApiResponse<List<FinancialProvider>>> getProviders();

  @GET('/accounts/providers/country/{countryCode}')
  Future<ApiResponse<List<FinancialProvider>>> getProvidersByCountry(@Path() String countryCode);

  // Savings Vaults
  @GET('/savings/vaults')
  Future<ApiResponse<List<SavingsVault>>> getVaults();

  @GET('/savings/vaults/active')
  Future<ApiResponse<List<SavingsVault>>> getActiveVaults();

  @GET('/savings/vaults/locked')
  Future<ApiResponse<List<SavingsVault>>> getLockedVaults();

  @GET('/savings/vaults/{vaultId}')
  Future<ApiResponse<SavingsVault>> getVault(@Path() String vaultId);

  @POST('/savings/vaults')
  Future<ApiResponse<SavingsVault>> createVault(@Body() CreateVaultRequest request);

  @PATCH('/savings/vaults/{vaultId}')
  Future<ApiResponse<SavingsVault>> updateVault(@Path() String vaultId, @Body() UpdateVaultRequest request);

  @DELETE('/savings/vaults/{vaultId}')
  Future<ApiResponse<void>> closeVault(@Path() String vaultId);

  // Savings Rules
  @GET('/savings/rules')
  Future<ApiResponse<List<SavingsRule>>> getRules();

  @GET('/savings/rules/active')
  Future<ApiResponse<List<SavingsRule>>> getActiveRules();

  @POST('/savings/rules')
  Future<ApiResponse<SavingsRule>> createRule(@Body() CreateRuleRequest request);

  @PATCH('/savings/rules/{ruleId}')
  Future<ApiResponse<SavingsRule>> updateRule(@Path() String ruleId, @Body() UpdateRuleRequest request);

  @POST('/savings/rules/{ruleId}/pause')
  Future<ApiResponse<SavingsRule>> pauseRule(@Path() String ruleId, @Body() PauseRuleRequest request);

  @POST('/savings/rules/{ruleId}/resume')
  Future<ApiResponse<SavingsRule>> resumeRule(@Path() String ruleId);

  @DELETE('/savings/rules/{ruleId}')
  Future<ApiResponse<void>> deleteRule(@Path() String ruleId);

  // Goals
  @GET('/goals')
  Future<ApiResponse<List<SavingsGoal>>> getGoals();

  @GET('/goals/active')
  Future<ApiResponse<List<SavingsGoal>>> getActiveGoals();

  @GET('/goals/upcoming')
  Future<ApiResponse<List<SavingsGoal>>> getUpcomingGoals(@Query('days') int days);

  @GET('/goals/{goalId}')
  Future<ApiResponse<SavingsGoal>> getGoal(@Path() String goalId);

  @POST('/goals')
  Future<ApiResponse<SavingsGoal>> createGoal(@Body() CreateGoalRequest request);

  @PATCH('/goals/{goalId}')
  Future<ApiResponse<SavingsGoal>> updateGoal(@Path() String goalId, @Body() UpdateGoalRequest request);

  @DELETE('/goals/{goalId}')
  Future<ApiResponse<void>> deleteGoal(@Path() String goalId);

  // Transactions
  @GET('/transactions')
  Future<ApiResponse<PaginatedResponse<Transaction>>> getTransactions({
    @Query('page') int page = 0,
    @Query('size') int size = 20,
    @Query('type') String? type,
    @Query('status') String? status,
    @Query('startDate') String? startDate,
    @Query('endDate') String? endDate,
  });

  @GET('/transactions/{transactionId}')
  Future<ApiResponse<Transaction>> getTransaction(@Path() String transactionId);

  @GET('/transactions/income')
  Future<ApiResponse<PaginatedResponse<IncomeTransaction>>> getIncomeTransactions({
    @Query('page') int page = 0,
    @Query('size') int size = 20,
  });

  @GET('/transactions/savings')
  Future<ApiResponse<PaginatedResponse<SavingsTransaction>>> getSavingsTransactions({
    @Query('page') int page = 0,
    @Query('size') int size = 20,
  });

  // Analytics
  @GET('/analytics/dashboard')
  Future<ApiResponse<DashboardSummary>> getDashboardSummary();

  @GET('/analytics')
  Future<ApiResponse<AnalyticsData>> getAnalytics(@Query('months') int months);

  // Notifications
  @GET('/notifications')
  Future<ApiResponse<PaginatedResponse<Notification>>> getNotifications({
    @Query('page') int page = 0,
    @Query('size') int size = 20,
    @Query('unreadOnly') bool? unreadOnly,
  });

  @GET('/notifications/unread-count')
  Future<ApiResponse<int>> getUnreadCount();

  @PATCH('/notifications/{notificationId}/read')
  Future<ApiResponse<void>> markAsRead(@Path() String notificationId);

  @POST('/notifications/read-all')
  Future<ApiResponse<void>> markAllAsRead();

  // Profile
  @GET('/profile')
  Future<ApiResponse<UserProfile>> getProfile();

  @PATCH('/profile')
  Future<ApiResponse<UserProfile>> updateProfile(@Body() UpdateProfileRequest request);

  @POST('/profile/change-pin')
  Future<ApiResponse<void>> changePin(@Body() ChangePinRequest request);

  @POST('/profile/biometrics')
  Future<ApiResponse<void>> toggleBiometrics(@Body() BiometricsRequest request);

  @PATCH('/profile/notifications')
  Future<ApiResponse<void>> updateNotificationPreferences(@Body() NotificationPreferencesRequest request);

  // Webhooks (Mock)
  @POST('/webhooks/mock/simulate-income')
  Future<ApiResponse<String>> simulateIncome({
    @Query('accountId') String accountId,
    @Query('amount') String amount,
    @Query('source') String source,
    @Query('category') String? category,
  });

  @POST('/webhooks/mock/simulate-salary')
  Future<ApiResponse<String>> simulateSalary({
    @Query('accountId') String accountId,
    @Query('amount') String amount,
  });

  @POST('/webhooks/mock/simulate-freelance')
  Future<ApiResponse<String>> simulateFreelance({
    @Query('accountId') String accountId,
    @Query('amount') String amount,
    @Query('client') String client,
  });

  @POST('/webhooks/mock/simulate-bonus')
  Future<ApiResponse<String>> simulateBonus({
    @Query('accountId') String accountId,
    @Query('amount') String amount,
    @Query('source') String source,
  });
}