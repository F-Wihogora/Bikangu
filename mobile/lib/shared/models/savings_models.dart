import 'package:freezed_annotation/freezed_annotation.dart';

part 'savings_models.freezed.dart';
part 'savings_models.g.dart';

@freezed
class SavingsVault with _$SavingsVault {
  const factory SavingsVault({
    required String id,
    required String userId,
    required String name,
    String? description,
    required String targetAmount,
    required String currentAmount,
    required String currency,
    String? icon,
    String? color,
    required DateTime startDate,
    DateTime? lockDate,
    DateTime? unlockDate,
    required String status,
    String? interestRate,
    DateTime? interestCalculatedAt,
    required bool autoLock,
    required bool allowEarlyWithdrawal,
    String? earlyWithdrawalPenaltyRate,
    String? minimumBalance,
  }) = _SavingsVault;

  factory SavingsVault.fromJson(Map<String, dynamic> json) => _$SavingsVaultFromJson(json);

  double get progressPercentage {
    final target = double.tryParse(targetAmount) ?? 0;
    final current = double.tryParse(currentAmount) ?? 0;
    if (target == 0) return 0;
    return (current / target) * 100;
  }

  double get remainingAmount {
    final target = double.tryParse(targetAmount) ?? 0;
    final current = double.tryParse(currentAmount) ?? 0;
    return target - current;
  }

  bool get isLocked => status == 'LOCKED' || (lockDate != null && DateTime.now().isAfter(lockDate!));
}

@freezed
class CreateVaultRequest with _$CreateVaultRequest {
  const factory CreateVaultRequest({
    required String name,
    String? description,
    required String targetAmount,
    required String currency,
    String? icon,
    String? color,
    DateTime? startDate,
    DateTime? lockDate,
    DateTime? unlockDate,
    String? interestRate,
    bool? autoLock,
    bool? allowEarlyWithdrawal,
    String? earlyWithdrawalPenaltyRate,
    String? minimumBalance,
  }) = _CreateVaultRequest;

  factory CreateVaultRequest.fromJson(Map<String, dynamic> json) => _$CreateVaultRequestFromJson(json);
}

@freezed
class UpdateVaultRequest with _$UpdateVaultRequest {
  const factory UpdateVaultRequest({
    String? name,
    String? description,
    String? targetAmount,
    String? icon,
    String? color,
    DateTime? lockDate,
    DateTime? unlockDate,
    String? interestRate,
    bool? autoLock,
    bool? allowEarlyWithdrawal,
    String? earlyWithdrawalPenaltyRate,
    String? minimumBalance,
  }) = _UpdateVaultRequest;

  factory UpdateVaultRequest.fromJson(Map<String, dynamic> json) => _$UpdateVaultRequestFromJson(json);
}

@freezed
class SavingsRule with _$SavingsRule {
  const factory SavingsRule({
    required String id,
    required String userId,
    required String vaultId,
    required String name,
    String? description,
    required String ruleType,
    String? percentage,
    String? fixedAmount,
    String? minIncomeThreshold,
    String? maxIncomeThreshold,
    String? incomeSource,
    List<String>? incomeSources,
    required int priority,
    required bool isActive,
    required bool isPaused,
    DateTime? pausedAt,
    String? pauseReason,
    required int executionCount,
    DateTime? lastExecutedAt,
    required String totalSaved,
  }) = _SavingsRule;

  factory SavingsRule.fromJson(Map<String, dynamic> json) => _$SavingsRuleFromJson(json);
}

@freezed
class CreateRuleRequest with _$CreateRuleRequest {
  const factory CreateRuleRequest({
    required String vaultId,
    required String name,
    String? description,
    required String ruleType,
    String? percentage,
    String? fixedAmount,
    String? minIncomeThreshold,
    String? maxIncomeThreshold,
    String? incomeSource,
    List<String>? incomeSources,
    int? priority,
  }) = _CreateRuleRequest;

  factory CreateRuleRequest.fromJson(Map<String, dynamic> json) => _$CreateRuleRequestFromJson(json);
}

@freezed
class UpdateRuleRequest with _$UpdateRuleRequest {
  const factory UpdateRuleRequest({
    String? name,
    String? description,
    String? ruleType,
    String? percentage,
    String? fixedAmount,
    String? minIncomeThreshold,
    String? maxIncomeThreshold,
    String? incomeSource,
    List<String>? incomeSources,
    int? priority,
    bool? isActive,
  }) = _UpdateRuleRequest;

  factory UpdateRuleRequest.fromJson(Map<String, dynamic> json) => _$UpdateRuleRequestFromJson(json);
}

@freezed
class PauseRuleRequest with _$PauseRuleRequest {
  const factory PauseRuleRequest({
    required String reason,
  }) = _PauseRuleRequest;

  factory PauseRuleRequest.fromJson(Map<String, dynamic> json) => _$PauseRuleRequestFromJson(json);
}