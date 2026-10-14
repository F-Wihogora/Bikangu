class AppConstants {
  static const String appName = 'Bikangu';
  static const String appVersion = '1.0.0';
  static const String baseUrl = 'http://10.0.2.2:8080/api/v1';
  static const String wsUrl = 'ws://10.0.2.2:8080/ws';

  static const Duration apiTimeout = Duration(seconds: 30);
  static const Duration connectTimeout = Duration(seconds: 10);

  static const String currencyCode = 'RWF';
  static const String currencySymbol = 'RWF';
  static const int currencyDecimalDigits = 0;

  static const int pinLength = 6;
  static const int maxLoginAttempts = 5;

  static const String secureStorageAccessTokenKey = 'access_token';
  static const String secureStorageRefreshTokenKey = 'refresh_token';
  static const String secureStorageUserIdKey = 'user_id';
  static const String secureStoragePinHashKey = 'pin_hash';
  static const String secureStorageBiometricsEnabledKey = 'biometrics_enabled';
  static const String secureStorageDeviceIdKey = 'device_id';

  static const String sharedPrefsOnboardingCompleteKey = 'onboarding_complete';
  static const String sharedPrefsThemeModeKey = 'theme_mode';
  static const String sharedPrefsLanguageKey = 'language';
  static const String sharedPrefsCurrencyKey = 'currency';
}