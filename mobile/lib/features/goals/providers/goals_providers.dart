import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:bikangu/shared/models/goal_models.dart';
import 'package:bikangu/core/providers/core_providers.dart';

class GoalsState {
  final List<SavingsGoal> goals;
  final bool isLoading;
  final String? error;

  const GoalsState({
    this.goals = const [],
    this.isLoading = false,
    this.error,
  });

  GoalsState copyWith({
    List<SavingsGoal>? goals,
    bool? isLoading,
    String? error,
  }) {
    return GoalsState(
      goals: goals ?? this.goals,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }

  List<SavingsGoal> get activeGoals => goals.where((g) => g.status == 'ACTIVE' || g.status == 'OVERDUE').toList();
  List<SavingsGoal> get completedGoals => goals.where((g) => g.isCompleted).toList();
}

class GoalsNotifier extends StateNotifier<GoalsState> {
  final ApiService _apiService;

  GoalsNotifier(this._apiService) : super(const GoalsState());

  Future<void> loadGoals() async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.getGoals();
      if (response.success && response.data != null) {
        state = state.copyWith(goals: response.data!, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load goals');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> loadActiveGoals() async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.getActiveGoals();
      if (response.success && response.data != null) {
        state = state.copyWith(goals: response.data!, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load goals');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> loadUpcomingGoals({int days = 30}) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.getUpcomingGoals(days);
      if (response.success && response.data != null) {
        state = state.copyWith(goals: response.data!, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load goals');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<SavingsGoal?> createGoal(CreateGoalRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.createGoal(request);
      if (response.success && response.data != null) {
        final newGoal = response.data!;
        state = state.copyWith(
          goals: [...state.goals, newGoal],
          isLoading: false,
        );
        return newGoal;
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to create goal');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
    return null;
  }

  Future<SavingsGoal?> updateGoal(String goalId, UpdateGoalRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.updateGoal(goalId, request);
      if (response.success && response.data != null) {
        final updatedGoal = response.data!;
        state = state.copyWith(
          goals: state.goals.map((g) => g.id == goalId ? updatedGoal : g).toList(),
          isLoading: false,
        );
        return updatedGoal;
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to update goal');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
    return null;
  }

  Future<void> deleteGoal(String goalId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.deleteGoal(goalId);
      if (response.success) {
        state = state.copyWith(
          goals: state.goals.where((g) => g.id != goalId).toList(),
          isLoading: false,
        );
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to delete goal');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }
}

final goalsProvider = StateNotifierProvider<GoalsNotifier, GoalsState>((ref) {
  final apiService = ref.watch(apiServiceProvider);
  return GoalsNotifier(apiService);
});