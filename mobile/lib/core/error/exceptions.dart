abstract class AppException implements Exception {
  final String message;
  final String code;
  final int? statusCode;

  const AppException(this.message, this.code, [this.statusCode]);

  @override
  String toString() => 'AppException: $code - $message';
}

class NetworkException extends AppException {
  const NetworkException(String message, [int? statusCode])
      : super(message, 'NETWORK_ERROR', statusCode);
}

class ServerException extends AppException {
  const ServerException(String message, [int? statusCode])
      : super(message, 'SERVER_ERROR', statusCode);
}

class AuthException extends AppException {
  const AuthException(String message, [int? statusCode])
      : super(message, 'AUTH_ERROR', statusCode);
}

class ValidationException extends AppException {
  final Map<String, String>? errors;

  const ValidationException(String message, [this.errors, int? statusCode])
      : super(message, 'VALIDATION_ERROR', statusCode);
}

class NotFoundException extends AppException {
  const NotFoundException(String message, [int? statusCode])
      : super(message, 'NOT_FOUND', statusCode);
}

class ConflictException extends AppException {
  const ConflictException(String message, [int? statusCode])
      : super(message, 'CONFLICT', statusCode);
}

class InsufficientFundsException extends AppException {
  const InsufficientFundsException(String message, [int? statusCode])
      : super(message, 'INSUFFICIENT_FUNDS', statusCode);
}

class IdempotencyException extends AppException {
  const IdempotencyException(String message, [int? statusCode])
      : super(message, 'IDEMPOTENCY_ERROR', statusCode);
}

class ProviderException extends AppException {
  const ProviderException(String message, [int? statusCode])
      : super(message, 'PROVIDER_ERROR', statusCode);
}

class CacheException extends AppException {
  const CacheException(String message, [int? statusCode])
      : super(message, 'CACHE_ERROR', statusCode);
}