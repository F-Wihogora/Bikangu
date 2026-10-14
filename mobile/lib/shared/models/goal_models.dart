import 'package:freezed_annotation/freezed_annotation.dart';

part 'goal_models.freezed.dart';
part 'goal_models.g.dart';

@freezed
class SavingsGoal with _$SavingsGoal {
  const factory SavingsGoal({
    required String id,
    required String userId,
    String? vaultId,
    required String name,
    String? description,
    required String targetAmount,
    required String currentAmount,
    required String currency,
    String? icon,
    String? color,
    DateTime? deadline,
    DateTime? lockDate,
    required String status,
    required int priority,
    required bool autoContribute,
    String? contributionFrequency,
    String? contributionAmount,
  }) = _SavingsGoal;

  factory SavingsGoal.fromJson(Map<String, dynamic> json) => _$SavingsGoalFromJson(json);

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

  bool get isCompleted => remainingAmount <= 0;
  bool get isOverdue => deadline != null && DateTime.now().isAfter(deadline!) && !isCompleted;
}

@freezed
class CreateGoalRequest with _$CreateGoalRequest {
  const factory CreateGoalRequest({
    String? vaultId,
    required String name,
    String? description,
    required String targetAmount,
    required String currency,
    String? icon,
    String? color,
    DateTime? deadline,
    DateTime? lockDate,
    int? priority,
    bool? autoContribute,
    String? contributionFrequency,
    String? contributionAmount,
  }) = _CreateGoalRequest;

  factory CreateGoalRequest.fromJson(Map<String, dynamic> json) => _$CreateGoalRequestFromJson(json);
}

@freezed
class UpdateGoalRequest with _$UpdateGoalRequest {
  const factory UpdateGoalRequest({
    String? name,
    String? description,
    String? targetAmount,
    String? icon,
    String? color,
    DateTime? deadline,
    DateTime? lockDate,
    int? priority,
    bool? autoContribute,
    String? contributionFrequency,
    String? contributionAmount,
  }) = _UpdateGoalRequest;

  factory UpdateGoalRequest.fromJson(Map<String, dynamic> json) => _$UpdateGoalRequestFromJson(json);
}