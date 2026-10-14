import 'package:freezed_annotation/freezed_annotation.dart';

part 'notification_models.freezed.dart';
part 'notification_models.g.dart';

@freezed
class Notification with _$Notification {
  const factory Notification({
    required String id,
    required String userId,
    required String type,
    String? category,
    required String title,
    required String message,
    required String priority,
    List<String>? channels,
    String? data,
    String? actionUrl,
    String? actionLabel,
    required bool isRead,
    DateTime? readAt,
    DateTime? sentAt,
    DateTime? deliveredAt,
    DateTime? failedAt,
    String? failureReason,
    String? fcmMessageId,
  }) = _Notification;

  factory Notification.fromJson(Map<String, dynamic> json) => _$NotificationFromJson(json);
}