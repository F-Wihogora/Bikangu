import 'package:freezed_annotation/freezed_annotation.dart';

part 'transaction_models.freezed.dart';
part 'transaction_models.g.dart';

@freezed
class Transaction with _$Transaction {
  const factory Transaction({
    required String id,
    required String userId,
    String? financialAccountId,
    String? vaultId,
    String? goalId,
    String? providerId,
    String? externalTransactionId,
    String? idempotencyKey,
    required String transactionType,
    String? transactionCategory,
    required String status,
    required String amount,
    required String currency,
    String? exchangeRate,
    String? baseAmount,
    String? baseCurrency,
    String? feeAmount,
    String? netAmount,
    String? description,
    String? reference,
    String? providerReference,
    String? incomeSource,
    String? incomeCategory,
    DateTime? processedAt,
    DateTime? completedAt,
    DateTime? failedAt,
    String? failureReason,
    int? retryCount,
    int? maxRetries,
  }) = _Transaction;

  factory Transaction.fromJson(Map<String, dynamic> json) => _$TransactionFromJson(json);
}

@freezed
class IncomeTransaction with _$IncomeTransaction {
  const factory IncomeTransaction({
    required String id,
    required String transactionId,
    required String userId,
    required String financialAccountId,
    required String providerId,
    required String externalTransactionId,
    required String amount,
    required String currency,
    required String incomeSource,
    String? incomeCategory,
    required DateTime receivedAt,
    required DateTime detectedAt,
    required bool isQualified,
    String? qualificationReason,
    required bool processed,
    DateTime? processedAt,
    String? savingsCalculated,
  }) = _IncomeTransaction;

  factory IncomeTransaction.fromJson(Map<String, dynamic> json) => _$IncomeTransactionFromJson(json);
}

@freezed
class SavingsTransaction with _$SavingsTransaction {
  const factory SavingsTransaction({
    required String id,
    required String transactionId,
    required String userId,
    required String vaultId,
    String? ruleId,
    String? goalId,
    required String financialAccountId,
    required String providerId,
    String? externalTransferId,
    required String amount,
    required String currency,
    String? feeAmount,
    String? netAmount,
    required String status,
    String? transferStatus,
    DateTime? initiatedAt,
    DateTime? completedAt,
    DateTime? failedAt,
    String? failureReason,
    required bool receiptGenerated,
    String? receiptUrl,
  }) = _SavingsTransaction;

  factory SavingsTransaction.fromJson(Map<String, dynamic> json) => _$SavingsTransactionFromJson(json);
}