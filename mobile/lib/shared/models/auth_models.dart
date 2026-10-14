import 'package:freezed_annotation/freezed_annotation.dart';

part 'auth_models.freezed.dart';
part 'auth_models.g.dart';

@freezed
class RegisterRequest with _$RegisterRequest {
  const factory RegisterRequest({
    required String email,
    String? phoneNumber,
    required String password,
    required String firstName,
    required String lastName,
    DateTime? dateOfBirth,
    String? gender,
    String? nationality,
  }) = _RegisterRequest;

  factory RegisterRequest.fromJson(Map<String, dynamic> json) => _$RegisterRequestFromJson(json);
}

@freezed
class LoginRequest with _$LoginRequest {
  const factory LoginRequest({
    required String email,
    required String password,
  }) = _LoginRequest;

  factory LoginRequest.fromJson(Map<String, dynamic> json) => _$LoginRequestFromJson(json);
}

@freezed
class RefreshTokenRequest with _$RefreshTokenRequest {
  const factory RefreshTokenRequest({
    required String refreshToken,
  }) = _RefreshTokenRequest;

  factory RefreshTokenRequest.fromJson(Map<String, dynamic> json) => _$RefreshTokenRequestFromJson(json);
}

@freezed
class VerifyEmailRequest with _$VerifyEmailRequest {
  const factory VerifyEmailRequest({
    required String token,
  }) = _VerifyEmailRequest;

  factory VerifyEmailRequest.fromJson(Map<String, dynamic> json) => _$VerifyEmailRequestFromJson(json);
}

@freezed
class VerifyPhoneRequest with _$VerifyPhoneRequest {
  const factory VerifyPhoneRequest({
    required String phoneNumber,
    required String otp,
  }) = _VerifyPhoneRequest;

  factory VerifyPhoneRequest.fromJson(Map<String, dynamic> json) => _$VerifyPhoneRequestFromJson(json);
}

@freezed
class ForgotPasswordRequest with _$ForgotPasswordRequest {
  const factory ForgotPasswordRequest({
    required String email,
  }) = _ForgotPasswordRequest;

  factory ForgotPasswordRequest.fromJson(Map<String, dynamic> json) => _$ForgotPasswordRequestFromJson(json);
}

@freezed
class ResetPasswordRequest with _$ResetPasswordRequest {
  const factory ResetPasswordRequest({
    required String token,
    required String newPassword,
  }) = _ResetPasswordRequest;

  factory ResetPasswordRequest.fromJson(Map<String, dynamic> json) => _$ResetPasswordRequestFromJson(json);
}

@freezed
class ChangePasswordRequest with _$ChangePasswordRequest {
  const factory ChangePasswordRequest({
    required String currentPassword,
    required String newPassword,
  }) = _ChangePasswordRequest;

  factory ChangePasswordRequest.fromJson(Map<String, dynamic> json) => _$ChangePasswordRequestFromJson(json);
}

@freezed
class SetPinRequest with _$SetPinRequest {
  const factory SetPinRequest({
    required String pin,
  }) = _SetPinRequest;

  factory SetPinRequest.fromJson(Map<String, dynamic> json) => _$SetPinRequestFromJson(json);
}

@freezed
class AuthResponse with _$AuthResponse {
  const factory AuthResponse({
    required String userId,
    required String email,
    required String firstName,
    required String lastName,
    required bool emailVerified,
    required bool phoneVerified,
    required String message,
  }) = _AuthResponse;

  factory AuthResponse.fromJson(Map<String, dynamic> json) => _$AuthResponseFromJson(json);
}

@freezed
class TokenResponse with _$TokenResponse {
  const factory TokenResponse({
    required String accessToken,
    required String refreshToken,
    required int expiresIn,
  }) = _TokenResponse;

  factory TokenResponse.fromJson(Map<String, dynamic> json) => _$TokenResponseFromJson(json);
}