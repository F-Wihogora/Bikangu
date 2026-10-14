import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:bikangu/core/network/api_service.dart';
import 'package:bikangu/core/storage/secure_storage.dart';
import 'package:bikangu/core/storage/preferences.dart';
import 'package:bikangu/shared/models/auth_models.dart';
import 'package:bikangu/shared/models/user_models.dart';

class AuthState {
  final bool isAuthenticated;
  final bool isLoading;
  final String? accessToken;
  final String? refreshToken;
  final String? userId;
  final UserProfile? userProfile;
  final bool isOnboardingComplete;
  final String? error;

  const AuthState({
    this.isAuthenticated = false,
    this.isLoading = false,
    this.accessToken,
    this.refreshToken,
    this.userId,
    this.userProfile,
    this.isOnboardingComplete = false,
    this.error,
  });

  AuthState copyWith({
    bool? isAuthenticated,
    bool? isLoading,
    String? accessToken,
    String? refreshToken,
    String? userId,
    UserProfile? userProfile,
    bool? isOnboardingComplete,
    String? error,
    bool clearTokens = false,
  }) {
    return AuthState(
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      isLoading: isLoading ?? this.isLoading,
      accessToken: clearTokens ? null : (accessToken ?? this.accessToken),
      refreshToken: clearTokens ? null : (refreshToken ?? this.refreshToken),
      userId: userId ?? this.userId,
      userProfile: userProfile ?? this.userProfile,
      isOnboardingComplete: isOnboardingComplete ?? this.isOnboardingComplete,
      error: error,
    );
  }
}

class AuthNotifier extends StateNotifier<AuthState> {
  final ApiService _apiService;
  final SecureStorageService _secureStorage;
  final PreferencesService _preferences;

  AuthNotifier(this._apiService, this._secureStorage, this._preferences)
      : super(const AuthState()) {
    _initializeAuth();
  }

  Future<void> _initializeAuth() async {
    state = state.copyWith(isLoading: true);

    try {
      final accessToken = await _secureStorage.getAccessToken();
      final refreshToken = await _secureStorage.getRefreshToken();
      final userId = await _secureStorage.getUserId();
      final onboardingComplete = _preferences.getOnboardingComplete();

      if (accessToken != null && refreshToken != null && userId != null) {
        // Verify token is still valid by fetching profile
        final profile = await _fetchProfile(accessToken);
        if (profile != null) {
          state = state.copyWith(
            isAuthenticated: true,
            accessToken: accessToken,
            refreshToken: refreshToken,
            userId: userId,
            userProfile: profile,
            isOnboardingComplete: onboardingComplete,
            isLoading: false,
          );
          return;
        }
      }

      // Clear invalid tokens
      await _clearAuthData();
      state = state.copyWith(isLoading: false);
    } catch (e) {
      await _clearAuthData();
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<UserProfile?> _fetchProfile(String accessToken) async {
    try {
      final response = await _apiService.getProfile();
      if (response.success && response.data != null) {
        return response.data!;
      }
    } catch (e) {
      return null;
    }
    return null;
  }

  Future<void> _clearAuthData() async {
    await _secureStorage.clearAuthData();
    state = state.copyWith(clearTokens: true, isLoading: false);
  }

  Future<void> register(RegisterRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.register(request);
      if (response.success && response.data != null) {
        state = state.copyWith(
          isLoading: false,
          error: null,
        );
      } else {
        state = state.copyWith(
          isLoading: false,
          error: response.message ?? 'Registration failed',
        );
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> login(LoginRequest request) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final response = await _apiService.login(request);
      if (response.success && response.data != null) {
        final data = response.data!;
        await _secureStorage.saveAccessToken(data.accessToken);
        await _secureStorage.saveRefreshToken(data.refreshToken);

        // Fetch user profile
        final profile = await _fetchProfile(data.accessToken);
        final userId = profile?.id;

        if (userId != null) {
          await _secureStorage.saveUserId(userId);
        }

        state = state.copyWith(
          isAuthenticated: true,
          isLoading: false,
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
          userId: userId,
          userProfile: profile,
          error: null,
        );
      } else {
        state = state.copyWith(
          isLoading: false,
          error: response.message ?? 'Login failed',
        );
      }
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> logout() async {
    try {
      final refreshToken = state.refreshToken;
      if (refreshToken != null) {
        await _apiService.logout(RefreshTokenRequest(refreshToken: refreshToken));
      }
    } catch (e) {
      // Ignore logout errors
    } finally {
      await _clearAuthData();
    }
  }

  Future<void> logoutAll() async {
    try {
      await _apiService.logoutAll();
    } catch (e) {
      // Ignore
    } finally {
      await _clearAuthData();
    }
  }

  Future<void> refreshToken() async {
    final refreshToken = state.refreshToken;
    if (refreshToken == null) return;

    try {
      final response = await _apiService.refreshToken(RefreshTokenRequest(refreshToken: refreshToken));
      if (response.success && response.data != null) {
        final data = response.data!;
        await _secureStorage.saveAccessToken(data.accessToken);
        await _secureStorage.saveRefreshToken(data.refreshToken);
        state = state.copyWith(
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
        );
      }
    } catch (e) {
      await logout();
    }
  }

  Future<void> verifyEmail(String token) async {
    try {
      await _apiService.verifyEmail(VerifyEmailRequest(token: token));
      if (state.userProfile != null) {
        state = state.copyWith(
          userProfile: state.userProfile!.copyWith(isEmailVerified: true),
        );
      }
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> verifyPhone(String phoneNumber, String otp) async {
    try {
      await _apiService.verifyPhone(VerifyPhoneRequest(phoneNumber: phoneNumber, otp: otp));
      if (state.userProfile != null) {
        state = state.copyWith(
          userProfile: state.userProfile!.copyWith(isPhoneVerified: true),
        );
      }
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> forgotPassword(String email) async {
    try {
      await _apiService.forgotPassword(ForgotPasswordRequest(email: email));
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> resetPassword(String token, String newPassword) async {
    try {
      await _apiService.resetPassword(ResetPasswordRequest(token: token, newPassword: newPassword));
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> changePassword(String currentPassword, String newPassword) async {
    try {
      await _apiService.changePassword(ChangePasswordRequest(currentPassword: currentPassword, newPassword: newPassword));
      await logoutAll();
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> setPin(String pin) async {
    try {
      await _apiService.setPin(SetPinRequest(pin: pin));
      // Store PIN hash locally
      // await _secureStorage.savePinHash(hashPin(pin));
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> completeOnboarding() async {
    await _preferences.setOnboardingComplete(true);
    state = state.copyWith(isOnboardingComplete: true);
  }

  Future<void> updateProfile(UserProfile profile) async {
    state = state.copyWith(userProfile: profile);
  }
}

final authProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  final apiService = ref.watch(apiServiceProvider);
  final secureStorage = ref.watch(secureStorageProvider);
  final preferences = ref.watch(preferencesProvider);
  return AuthNotifier(apiService, secureStorage, preferences);
});