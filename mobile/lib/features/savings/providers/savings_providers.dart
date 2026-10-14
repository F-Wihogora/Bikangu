import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:bikangu/shared/models/savings_models.dart';
import 'package:bikangu/core/providers/core_providers.dart';

class SavingsState {
  final List<SavingsVault> vaults;
  final List<SavingsRule> rules;
  final bool isLoading;
  final String? error;

  const SavingsState({
    this.vaults = const [],
    this.rules = const [],
    this.isLoading = false,
    this.error,
  });

  SavingsState copyWith({
    List<SavingsVault>? vaults,
    List<SavingsRule>? rules,
    bool? isLoading,
    String? error,
  }) {
    return SavingsState(
      vaults: vaults ?? this.vaults,
      rules: rules ?? this.rules,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }

  List<SavingsVault> get activeVaults => vaults.where((v) => v.status == 'ACTIVE').toList();
  List<SavingsVault> get lockedVaults => vaults.where((v) => v.isLocked).toList();
}

class SavingsNotifier extends StateNotifier<SavingsState> {
  final ApiService _apiService;

  SavingsNotifier(this._apiService) : super(const SavingsState());

  Future<void> loadVaults() async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.getVaults();
      if (response.success && response.data != null) {
        state = state.copyWith(vaults: response.data!, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load vaults');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> loadActiveVaults() async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.getActiveVaults();
      if (response.success && response.data != null) {
        state = state.copyWith(vaults: response.data!, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load vaults');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> loadLockedVaults() async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.getLockedVaults();
      if (response.success && response.data != null) {
        state = state.copyWith(vaults: response.data!, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load vaults');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<SavingsVault?> createVault(CreateVaultRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.createVault(request);
      if (response.success && response.data != null) {
        final newVault = response.data!;
        state = state.copyWith(
          vaults: [...state.vaults, newVault],
          isLoading: false,
        );
        return newVault;
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to create vault');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
    return null;
  }

  Future<SavingsVault?> updateVault(String vaultId, UpdateVaultRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.updateVault(vaultId, request);
      if (response.success && response.data != null) {
        final updatedVault = response.data!;
        state = state.copyWith(
          vaults: state.vaults.map((v) => v.id == vaultId ? updatedVault : v).toList(),
          isLoading: false,
        );
        return updatedVault;
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to update vault');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
    return null;
  }

  Future<void> closeVault(String vaultId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.closeVault(vaultId);
      if (response.success) {
        state = state.copyWith(
          vaults: state.vaults.map((v) => v.id == vaultId ? v.copyWith(status: 'CLOSED') : v).toList(),
          isLoading: false,
        );
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to close vault');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> loadRules() async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.getRules();
      if (response.success && response.data != null) {
        state = state.copyWith(rules: response.data!, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load rules');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> loadActiveRules() async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.getActiveRules();
      if (response.success && response.data != null) {
        state = state.copyWith(rules: response.data!, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load rules');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<SavingsRule?> createRule(CreateRuleRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.createRule(request);
      if (response.success && response.data != null) {
        final newRule = response.data!;
        state = state.copyWith(
          rules: [...state.rules, newRule],
          isLoading: false,
        );
        return newRule;
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to create rule');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
    return null;
  }

  Future<SavingsRule?> updateRule(String ruleId, UpdateRuleRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.updateRule(ruleId, request);
      if (response.success && response.data != null) {
        final updatedRule = response.data!;
        state = state.copyWith(
          rules: state.rules.map((r) => r.id == ruleId ? updatedRule : r).toList(),
          isLoading: false,
        );
        return updatedRule;
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to update rule');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
    return null;
  }

  Future<void> pauseRule(String ruleId, String reason) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.pauseRule(ruleId, PauseRuleRequest(reason: reason));
      if (response.success && response.data != null) {
        final updatedRule = response.data!;
        state = state.copyWith(
          rules: state.rules.map((r) => r.id == ruleId ? updatedRule : r).toList(),
          isLoading: false,
        );
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to pause rule');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> resumeRule(String ruleId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.resumeRule(ruleId);
      if (response.success && response.data != null) {
        final updatedRule = response.data!;
        state = state.copyWith(
          rules: state.rules.map((r) => r.id == ruleId ? updatedRule : r).toList(),
          isLoading: false,
        );
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to resume rule');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> deleteRule(String ruleId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.deleteRule(ruleId);
      if (response.success) {
        state = state.copyWith(
          rules: state.rules.where((r) => r.id != ruleId).toList(),
          isLoading: false,
        );
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to delete rule');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }
}

final savingsProvider = StateNotifierProvider<SavingsNotifier, SavingsState>((ref) {
  final apiService = ref.watch(apiServiceProvider);
  return SavingsNotifier(apiService);
});