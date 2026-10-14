import 'package:freezed_annotation/freezed_annotation.dart';

part 'analytics_models.freezed.dart';
part 'analytics_models.g.dart';

@freezed
class DashboardSummary with _$DashboardSummary {
  const factory DashboardSummary({
    required String totalSavings,
    required String thisMonthSavings,
    required String lockedSavings,
    required String availableBalance,
    required int activeGoalsCount,
    required int totalVaultsCount,
    required List<Transaction> recentTransactions,
    required List<SavingsGoal> activeGoals,
    required List<SavingsVault> vaults,
  }) = _DashboardSummary;

  factory DashboardSummary.fromJson(Map<String, dynamic> json) => _$DashboardSummaryFromJson(json);
}

@freezed
class AnalyticsData with _$AnalyticsData {
  const factory AnalyticsData({
    required List<MonthlySavings> monthlySavings,
    required List<SavingsByCategory> savingsByCategory,
  }) = _AnalyticsData;

  factory AnalyticsData.fromJson(Map<String, dynamic> json) => _$AnalyticsDataFromJson(json);
}

@freezed
class MonthlySavings with _$MonthlySavings {
  const factory MonthlySavings({
    required String month,
    required String income,
    required String savings,
    required String withdrawals,
    required String netSavings,
    required String savingsRate,
  }) = _MonthlySavings;

  factory MonthlySavings.fromJson(Map<String, dynamic> json) => _$MonthlySavingsFromJson(json);
}

@freezed
class SavingsByCategory with _$SavingsByCategory {
  const factory SavingsByCategory({
    required String category,
    required String amount,
  }) = _SavingsByCategory;

  factory SavingsByCategory.fromJson(Map<String, dynamic> json) => _$SavingsByCategoryFromJson(json);
}