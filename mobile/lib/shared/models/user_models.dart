import 'package:freezed_annotation/freezed_annotation.dart';

part 'user_models.freezed.dart';
part 'user_models.g.dart';

@freezed
class UserProfile with _$UserProfile {
  const factory UserProfile({
    required String id,
    required String email,
    String? phoneNumber,
    required String firstName,
    required String lastName,
    DateTime? dateOfBirth,
    String? gender,
    String? nationality,
    String? kycStatus,
    required bool isActive,
    required bool isEmailVerified,
    required bool isPhoneVerified,
    DateTime? lastLoginAt,
    String? preferredLanguage,
    String? preferredCurrency,
    String? timezone,
  }) = _UserProfile;

  factory UserProfile.fromJson(Map<String, dynamic> json) => _$UserProfileFromJson(json);

  String get fullName => '$firstName $lastName';
}

@freezed
class UpdateProfileRequest with _$UpdateProfileRequest {
  const factory UpdateProfileRequest({
    String? firstName,
    String? lastName,
    DateTime? dateOfBirth,
    String? gender,
    String? nationality,
    String? preferredLanguage,
    String? preferredCurrency,
    String? timezone,
  }) = _UpdateProfileRequest;

  factory UpdateProfileRequest.fromJson(Map<String, dynamic> json) => _$UpdateProfileRequestFromJson(json);
}

@freezed
class ChangePinRequest with _$ChangePinRequest {
  const factory ChangePinRequest({
    required String currentPin,
    required String newPin,
  }) = _ChangePinRequest;

  factory ChangePinRequest.fromJson(Map<String, dynamic> json) => _$ChangePinRequestFromJson(json);
}

@freezed
class BiometricsRequest with _$BiometricsRequest {
  const factory BiometricsRequest({
    required bool enabled,
  }) = _BiometricsRequest;

  factory BiometricsRequest.fromJson(Map<String, dynamic> json) => _$BiometricsRequestFromJson(json);
}

@freezed
class NotificationPreferencesRequest with _$NotificationPreferencesRequest {
  const factory NotificationPreferencesRequest({
    bool? emailEnabled,
    bool? pushEnabled,
    bool? inAppEnabled,
    bool? smsEnabled,
    bool? salaryReceived,
    bool? savingsExecuted,
    bool? savingsFailed,
    bool? goalAchieved,
    bool? goalNearingDeadline,
    bool? accountConnected,
    bool? accountDisconnected,
    bool? securityAlerts,
    bool? failedLogin,
    bool? passwordChanged,
    bool? pinChanged,
    bool? withdrawalRequested,
    bool? transferCompleted,
    bool? weeklySummary,
    bool? monthlyReport,
    bool? marketingEmails,
    String? quietHoursStart,
    String? quietHoursEnd,
    String? timezone,
  }) = _NotificationPreferencesRequest;

  factory NotificationPreferencesRequest.fromJson(Map<String, dynamic> json) => _$NotificationPreferencesRequestFromJson(json);
}