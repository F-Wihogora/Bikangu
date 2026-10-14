import 'package:freezed_annotation/freezed_annotation.dart';

part 'provider_models.freezed.dart';
part 'provider_models.g.dart';

@freezed
class ProviderAccount with _$ProviderAccount {
  const factory ProviderAccount({
    required String id,
    required String providerAccountId,
    String? accountType,
    required String accountName,
    String? accountNumberMasked,
    String? accountHolderName,
    required String currency,
    required String currentBalance,
    required String availableBalance,
    required bool isVerified,
    required bool isActive,
    DateTime? lastSyncAt,
    List<ProviderTransaction>? recentTransactions,
  }) = _ProviderAccount;

  factory ProviderAccount.fromJson(Map<String, dynamic> json) => _$ProviderAccountFromJson(json);
}

@freezed
class ProviderTransaction with _$ProviderTransaction {
  const factory ProviderTransaction({
    required String id,
    required String externalTransactionId,
    required String type,
    String? category,
    required String amount,
    required String currency,
    String? description,
    String? reference,
    required DateTime transactionDate,
    required String status,
    String? incomeSource,
    String? incomeCategory,
  }) = _ProviderTransaction;

  factory ProviderTransaction.fromJson(Map<String, dynamic> json) => _$ProviderTransactionFromJson(json);
}