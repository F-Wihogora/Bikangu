abstract class Failure {
  final String message;
  final String code;

  const Failure(this.message, this.code);

  @override
  String toString() => 'Failure: $code - $message';
}

class NetworkFailure extends Failure {
  const NetworkFailure(String message) : super(message, 'NETWORK_FAILURE');
}

class ServerFailure extends Failure {
  const ServerFailure(String message) : super(message, 'SERVER_FAILURE');
}

class AuthFailure extends Failure {
  const AuthFailure(String message) : super(message, 'AUTH_FAILURE');
}

class ValidationFailure extends Failure {
  final Map<String, String>? errors;

  const ValidationFailure(String message, [this.errors]) : super(message, 'VALIDATION_FAILURE');
}

class NotFoundFailure extends Failure {
  const NotFoundFailure(String message) : super(message, 'NOT_FOUND');
}

class ConflictFailure extends Failure {
  const ConflictFailure(String message) : super(message, 'CONFLICT');
}

class InsufficientFundsFailure extends Failure {
  const InsufficientFundsFailure(String message) : super(message, 'INSUFFICIENT_FUNDS');
}

class IdempotencyFailure extends Failure {
  const IdempotencyFailure(String message) : super(message, 'IDEMPOTENCY_FAILURE');
}

class ProviderFailure extends Failure {
  const ProviderFailure(String message) : super(message, 'PROVIDER_FAILURE');
}

class CacheFailure extends Failure {
  const CacheFailure(String message) : super(message, 'CACHE_FAILURE');
}

class UnknownFailure extends Failure {
  const UnknownFailure(String message) : super(message, 'UNKNOWN_FAILURE');
}