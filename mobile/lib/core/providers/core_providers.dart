import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:bikangu/core/network/api_client.dart';
import 'package:bikangu/core/network/api_service.dart';
import 'package:bikangu/core/storage/secure_storage.dart';
import 'package:bikangu/core/storage/preferences.dart';

final dioProvider = Provider<Dio>((ref) {
  final secureStorage = ref.watch(secureStorageProvider);
  // Dio is created in the NetworkModule with interceptors
  // This is a fallback
  return Dio();
});

final apiServiceProvider = Provider<ApiService>((ref) {
  final dio = ref.watch(dioProvider);
  return ApiService(dio);
});

final secureStorageProvider = Provider<FlutterSecureStorage>((ref) {
  return const FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
    iOptions: IOSOptions(accessibility: KeychainAccessibility.first_unlock_this_device),
  );
});

final secureStorageServiceProvider = Provider<SecureStorageService>((ref) {
  return SecureStorageService(ref.watch(secureStorageProvider));
});

final preferencesProvider = FutureProvider<SharedPreferences>((ref) async {
  return await SharedPreferences.getInstance();
});

final preferencesServiceProvider = Provider<PreferencesService>((ref) {
  // This will be overridden in main with the actual instance
  throw UnimplementedError('PreferencesService must be initialized in main');
});

final connectivityProvider = StreamProvider<bool>((ref) async* {
  // Implementation would use connectivity_plus package
  yield true;
  await Future.delayed(const Duration(seconds: 30));
});