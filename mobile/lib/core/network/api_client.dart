import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:injectable/injectable.dart';
import 'package:bikangu/core/constants/app_constants.dart';
import 'package:bikangu/core/error/exceptions.dart';

@module
abstract class NetworkModule {
  @singleton
  Dio dio(FlutterSecureStorage secureStorage) {
    final dio = Dio(BaseOptions(
      baseUrl: AppConstants.baseUrl,
      connectTimeout: AppConstants.connectTimeout,
      receiveTimeout: AppConstants.apiTimeout,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    ));

    dio.interceptors.add(AuthInterceptor(secureStorage));
    dio.interceptors.add(LoggingInterceptor());
    dio.interceptors.add(ErrorInterceptor());

    return dio;
  }
}

class AuthInterceptor extends Interceptor {
  final FlutterSecureStorage _secureStorage;

  AuthInterceptor(this._secureStorage);

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    final accessToken = await _secureStorage.read(key: AppConstants.secureStorageAccessTokenKey);
    if (accessToken != null) {
      options.headers['Authorization'] = 'Bearer $accessToken';
    }

    final deviceId = await _secureStorage.read(key: AppConstants.secureStorageDeviceIdKey);
    if (deviceId != null) {
      options.headers['X-Device-Id'] = deviceId;
    }

    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode == 401) {
      // Try to refresh token
      final refreshed = await _refreshToken(err.requestOptions);
      if (refreshed) {
        // Retry the original request
        final accessToken = await _secureStorage.read(key: AppConstants.secureStorageAccessTokenKey);
        if (accessToken != null) {
          err.requestOptions.headers['Authorization'] = 'Bearer $accessToken';
        }
        try {
          final response = await Dio().fetch(err.requestOptions);
          handler.resolve(response);
          return;
        } catch (e) {
          handler.next(err);
          return;
        }
      }
    }
    handler.next(err);
  }

  Future<bool> _refreshToken(RequestOptions options) async {
    try {
      final refreshToken = await _secureStorage.read(key: AppConstants.secureStorageRefreshTokenKey);
      if (refreshToken == null) return false;

      final response = await Dio().post(
        '${AppConstants.baseUrl}/auth/refresh',
        data: {'refreshToken': refreshToken},
      );

      if (response.statusCode == 200) {
        final data = response.data;
        await _secureStorage.write(key: AppConstants.secureStorageAccessTokenKey, value: data['accessToken']);
        await _secureStorage.write(key: AppConstants.secureStorageRefreshTokenKey, value: data['refreshToken']);
        return true;
      }
    } catch (e) {
      return false;
    }
    return false;
  }
}

class LoggingInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    print('���� REQUEST[${options.method}] => PATH: ${options.path}');
    print('Headers: ${options.headers}');
    if (options.data != null) {
      print('Body: ${options.data}');
    }
    handler.next(options);
  }

  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    print('��� RESPONSE[${response.statusCode}] => PATH: ${response.requestOptions.path}');
    handler.next(response);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    print('��� ERROR[${err.response?.statusCode}] => PATH: ${err.requestOptions.path}');
    print('Message: ${err.message}');
    if (err.response?.data != null) {
      print('Error Data: ${err.response?.data}');
    }
    handler.next(err);
  }
}

class ErrorInterceptor extends Interceptor {
  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    switch (err.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        handler.next(NetworkException('Connection timeout', err.response?.statusCode));
        break;
      case DioExceptionType.connectionError:
        handler.next(NetworkException('No internet connection', err.response?.statusCode));
        break;
      case DioExceptionType.badResponse:
        final statusCode = err.response?.statusCode;
        final data = err.response?.data;
        String message = 'Server error';
        String code = 'SERVER_ERROR';

        if (data is Map) {
          message = data['message'] ?? message;
          code = data['code'] ?? code;
        }

        switch (statusCode) {
          case 400:
            if (data is Map && data['details'] is Map) {
              final errors = Map<String, String>.from(data['details']);
              handler.next(ValidationException(message, errors, statusCode));
            } else {
              handler.next(ValidationException(message, null, statusCode));
            }
            break;
          case 401:
            handler.next(AuthException(message, statusCode));
            break;
          case 403:
            handler.next(AuthException('Access denied', statusCode));
            break;
          case 404:
            handler.next(NotFoundException(message, statusCode));
            break;
          case 409:
            if (code == 'IDEMPOTENCY_VIOLATION') {
              handler.next(IdempotencyException(message, statusCode));
            } else {
              handler.next(ConflictException(message, statusCode));
            }
            break;
          case 422:
            handler.next(ValidationException(message, null, statusCode));
            break;
          case 500:
            handler.next(ServerException(message, statusCode));
            break;
          default:
            handler.next(ServerException(message, statusCode));
        }
        break;
      default:
        handler.next(NetworkException('Network error: ${err.message}', err.response?.statusCode));
    }
  }
}