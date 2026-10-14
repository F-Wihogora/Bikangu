import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:bikangu/shared/models/account_models.dart';
import 'package:bikangu/core/providers/core_providers.dart';

class AccountsState {
  final List<FinancialAccount> accounts;
  final FinancialAccount? primaryAccount;
  final List<FinancialProvider> providers;
  final bool isLoading;
  final String? error;

  const AccountsState({
    this.accounts = const [],
    this.primaryAccount,
    this.providers = const [],
    this.isLoading = false,
    this.error,
  });

  AccountsState copyWith({
    List<FinancialAccount>? accounts,
    FinancialAccount? primaryAccount,
    List<FinancialProvider>? providers,
    bool? isLoading,
    String? error,
  }) {
    return AccountsState(
      accounts: accounts ?? this.accounts,
      primaryAccount: primaryAccount ?? this.primaryAccount,
      providers: providers ?? this.providers,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }
}

class AccountsNotifier extends StateNotifier<AccountsState> {
  final ApiService _apiService;

  AccountsNotifier(this._apiService) : super(const AccountsState());

  Future<void> loadAccounts() async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.getAccounts();
      if (response.success && response.data != null) {
        final accounts = response.data!;
        final primary = accounts.firstWhere(
          (a) => a.isPrimary,
          orElse: () => accounts.isNotEmpty ? accounts.first : null as FinancialAccount,
        );
        state = state.copyWith(accounts: accounts, primaryAccount: primary, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load accounts');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> loadPrimaryAccount() async {
    try {
      final response = await _apiService.getPrimaryAccount();
      if (response.success && response.data != null) {
        state = state.copyWith(primaryAccount: response.data);
      }
    } catch (e) {
      // Ignore
    }
  }

  Future<void> loadProviders({String? countryCode}) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = countryCode != null
          ? await _apiService.getProvidersByCountry(countryCode)
          : await _apiService.getProviders();
      if (response.success && response.data != null) {
        state = state.copyWith(providers: response.data!, isLoading: false);
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to load providers');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<FinancialAccount?> linkAccount(LinkAccountRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.linkAccount(request);
      if (response.success && response.data != null) {
        final newAccount = response.data!;
        final updatedAccounts = [...state.accounts, newAccount];
        FinancialAccount? primary = state.primaryAccount;
        if (newAccount.isPrimary) {
          primary = newAccount;
        }
        state = state.copyWith(
          accounts: updatedAccounts,
          primaryAccount: primary,
          isLoading: false,
        );
        return newAccount;
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to link account');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
    return null;
  }

  Future<FinancialAccount?> updateAccount(String accountId, UpdateAccountRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.updateAccount(accountId, request);
      if (response.success && response.data != null) {
        final updatedAccount = response.data!;
        final updatedAccounts = state.accounts.map((a) => a.id == accountId ? updatedAccount : a).toList();
        FinancialAccount? primary = state.primaryAccount;
        if (updatedAccount.isPrimary) {
          primary = updatedAccount;
        } else if (primary?.id == accountId && !updatedAccount.isPrimary) {
          primary = updatedAccounts.firstWhere((a) => a.isPrimary, orElse: () => updatedAccounts.first);
        }
        state = state.copyWith(
          accounts: updatedAccounts,
          primaryAccount: primary,
          isLoading: false,
        );
        return updatedAccount;
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to update account');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
    return null;
  }

  Future<void> unlinkAccount(String accountId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.unlinkAccount(accountId);
      if (response.success) {
        final updatedAccounts = state.accounts.where((a) => a.id != accountId).toList();
        FinancialAccount? primary = state.primaryAccount;
        if (primary?.id == accountId) {
          primary = updatedAccounts.firstWhere((a) => a.isPrimary, orElse: () => updatedAccounts.firstOrNull);
        }
        state = state.copyWith(
          accounts: updatedAccounts,
          primaryAccount: primary,
          isLoading: false,
        );
      } else {
        state = state.copyWith(isLoading: false, error: response.message ?? 'Failed to unlink account');
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<FinancialAccount?> syncBalance(String accountId) async {
    try {
      final response = await _apiService.syncBalance(accountId);
      if (response.success && response.data != null) {
        final updatedAccount = response.data!;
        state = state.copyWith(
          accounts: state.accounts.map((a) => a.id == accountId ? updatedAccount : a).toList(),
        );
        if (state.primaryAccount?.id == accountId) {
          state = state.copyWith(primaryAccount: updatedAccount);
        }
        return updatedAccount;
      }
    } catch (e) {
      // Ignore
    }
    return null;
  }
}

final accountsProvider = StateNotifierProvider<AccountsNotifier, AccountsState>((ref) {
  final apiService = ref.watch(apiServiceProvider);
  return AccountsNotifier(apiService);
});