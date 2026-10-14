import 'package:freezed_annotation/freezed_annotation.dart';

part 'account_models.freezed.dart';
part 'account_models.g.dart';

@freezed
class FinancialProvider with _$FinancialProvider {
  const factory FinancialProvider({
    required String id,
    required String name,
    required String displayName,
    required String providerType,
    required String category,
    required String countryCode,
    required List<String> currencyCodes,
    String? logoUrl,
    String? iconUrl,
    String? websiteUrl,
    required bool isActive,
    required bool isSandbox,
    required bool supportsAccountVerification,
    required bool supportsBalanceCheck,
    required bool supportsTransactionHistory,
    required bool supportsTransfer,
    required bool supportsWebhooks,
    int? rateLimitPerMinute,
  }) = _FinancialProvider;

  factory FinancialProvider.fromJson(Map<String, dynamic> json) => _$FinancialProviderFromJson(json);
}

@freezed
class FinancialAccount with _$FinancialAccount {
  const factory FinancialAccount({
    required String id,
    required String userId,
    required String providerId,
    required String providerAccountId,
    String? providerAccountType,
    required String accountName,
    String? accountNumberMasked,
    String? accountHolderName,
    required String currency,
    required bool isPrimary,
    required bool isVerified,
    required bool isActive,
    DateTime? lastSyncAt,
    DateTime? lastBalanceCheckAt,
    required String currentBalance,
    required String availableBalance,
    String? verificationStatus,
    int? verificationAttempts,
    DateTime? verifiedAt,
    String? verificationFailedReason,
  }) = _FinancialAccount;

  factory FinancialAccount.fromJson(Map<String, dynamic> json) => _$FinancialAccountFromJson(json);
}

@freezed
class LinkAccountRequest with _$LinkAccountRequest {
  const factory LinkAccountRequest({
    required String providerId,
    required String providerAccountId,
    required String credentials,
    String? accountName,
    required bool isPrimary,
  }) = _LinkAccountRequest;

  factory LinkAccountRequest.fromJson(Map<String, dynamic> json) => _$LinkAccountRequestFromJson(json);
}

@freezed
class UpdateAccountRequest with _$UpdateAccountRequest {
  const factory UpdateAccountRequest({
    String? accountName,
    bool? isPrimary,
    bool? isActive,
  }) = _UpdateAccountRequest;

  factory UpdateAccountRequest.fromJson(Map<String, dynamic> json) => _$UpdateAccountRequestFromJson(json);
}