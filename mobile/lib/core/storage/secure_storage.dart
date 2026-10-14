import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:injectable/injectable.dart';
import 'package:bikangu/core/constants/app_constants.dart';

@module
abstract class StorageModule {
  @singleton
  FlutterSecureStorage secureStorage() => const FlutterSecureStorage(
        aOptions: AndroidOptions(
          encryptedSharedPreferences: true,
        ),
        iOptions: IOSOptions(
          accessibility: KeychainAccessibility.first_unlock_this_device,
        ),
      );
}

class SecureStorageService {
  final FlutterSecureStorage _storage;

  SecureStorageService(this._storage);

  Future<void> saveAccessToken(String token) async {
    await _storage.write(key: AppConstants.secureStorageAccessTokenKey, value: token);
  }

  Future<String?> getAccessToken() async {
    return await _storage.read(key: AppConstants.secureStorageAccessTokenKey);
  }

  Future<void> saveRefreshToken(String token) async {
    await _storage.write(key: AppConstants.secureStorageRefreshTokenKey, value: token);
  }

  Future<String?> getRefreshToken() async {
    return await _storage.read(key: AppConstants.secureStorageRefreshTokenKey);
  }

  Future<void> saveUserId(String userId) async {
    await _storage.write(key: AppConstants.secureStorageUserIdKey, value: userId);
  }

  Future<String?> getUserId() async {
    return await _storage.read(key: AppConstants.secureStorageUserIdKey);
  }

  Future<void> savePinHash(String pinHash) async {
    await _storage.write(key: AppConstants.secureStoragePinHashKey, value: pinHash);
  }

  Future<String?> getPinHash() async {
    return await _storage.read(key: AppConstants.secureStoragePinHashKey);
  }

  Future<void> saveBiometricsEnabled(bool enabled) async {
    await _storage.write(
        key: AppConstants.secureStorageBiometricsEnabledKey,
        value: enabled.toString());
  }

  Future<bool> getBiometricsEnabled() async {
    final value = await _storage.read(key: AppConstants.secureStorageBiometricsEnabledKey);
    return value == 'true';
  }

  Future<void> saveDeviceId(String deviceId) async {
    await _storage.write(key: AppConstants.secureStorageDeviceIdKey, value: deviceId);
  }

  Future<String?> getDeviceId() async {
    return await _storage.read(key: AppConstants.secureStorageDeviceIdKey);
  }

  Future<void> clearAuthData() async {
    await _storage.delete(key: AppConstants.secureStorageAccessTokenKey);
    await _storage.delete(key: AppConstants.secureStorageRefreshTokenKey);
    await _storage.delete(key: AppConstants.secureStorageUserIdKey);
  }

  Future<void> clearAll() async {
    await _storage.deleteAll();
  }
}