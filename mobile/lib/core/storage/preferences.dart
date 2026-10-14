import 'package:shared_preferences/shared_preferences.dart';
import 'package:injectable/injectable.dart';
import 'package:bikangu/core/constants/app_constants.dart';

@module
abstract class PreferencesModule {
  @singleton
  Future<SharedPreferences> sharedPreferences() => SharedPreferences.getInstance();
}

class PreferencesService {
  final SharedPreferences _prefs;

  PreferencesService(this._prefs);

  Future<void> setOnboardingComplete(bool complete) async {
    await _prefs.setBool(AppConstants.sharedPrefsOnboardingCompleteKey, complete);
  }

  bool getOnboardingComplete() {
    return _prefs.getBool(AppConstants.sharedPrefsOnboardingCompleteKey) ?? false;
  }

  Future<void> setThemeMode(String themeMode) async {
    await _prefs.setString(AppConstants.sharedPrefsThemeModeKey, themeMode);
  }

  String getThemeMode() {
    return _prefs.getString(AppConstants.sharedPrefsThemeModeKey) ?? 'system';
  }

  Future<void> setLanguage(String language) async {
    await _prefs.setString(AppConstants.sharedPrefsLanguageKey, language);
  }

  String getLanguage() {
    return _prefs.getString(AppConstants.sharedPrefsLanguageKey) ?? 'en';
  }

  Future<void> setCurrency(String currency) async {
    await _prefs.setString(AppConstants.sharedPrefsCurrencyKey, currency);
  }

  String getCurrency() {
    return _prefs.getString(AppConstants.sharedPrefsCurrencyKey) ?? AppConstants.currencyCode;
  }
}