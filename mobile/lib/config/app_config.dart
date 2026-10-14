import 'package:firebase_core/firebase_core.dart';

class AppConfig {
  static const String apiBaseUrl = 'http://10.0.2.2:8080/api/v1';
  static const String wsBaseUrl = 'ws://10.0.2.2:8080/ws';

  static const FirebaseOptions firebaseOptions = FirebaseOptions(
    apiKey: 'YOUR_API_KEY',
    appId: 'YOUR_APP_ID',
    messagingSenderId: 'YOUR_SENDER_ID',
    projectId: 'YOUR_PROJECT_ID',
    storageBucket: 'YOUR_STORAGE_BUCKET',
  );

  static const String googleMapsApiKey = 'YOUR_GOOGLE_MAPS_API_KEY';
  static const String sentryDsn = 'YOUR_SENTRY_DSN';

  static const bool enableLogging = true;
  static const bool enableCrashlytics = false;
  static const bool enableAnalytics = false;

  static const Duration apiTimeout = Duration(seconds: 30);
  static const Duration connectTimeout = Duration(seconds: 10);

  static const String currencyCode = 'RWF';
  static const String currencySymbol = 'RWF';
  static const int currencyDecimalDigits = 0;

  static const String defaultLanguage = 'en';
  static const List<String> supportedLanguages = ['en', 'rw', 'fr'];

  static const int pinLength = 6;
  static const int maxLoginAttempts = 5;
  static const int lockoutDurationMinutes = 15;
}