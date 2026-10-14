import 'package:freezed_annotation/freezed_annotation.dart';

part 'api_response.freezed.dart';
part 'api_response.g.dart';

@freezed
class ApiResponse<T> with _$ApiResponse<T> {
  const factory ApiResponse({
    required bool success,
    T? data,
    String? message,
    Meta? meta,
  }) = _ApiResponse;

  factory ApiResponse.fromJson(Map<String, dynamic> json, T Function(dynamic) fromJsonT) =>
      _$ApiResponseFromJson(json, fromJsonT);
}

@freezed
class Meta with _$Meta {
  const factory Meta({
    required DateTime timestamp,
    String? requestId,
    Pagination? pagination,
  }) = _Meta;

  factory Meta.fromJson(Map<String, dynamic> json) => _$MetaFromJson(json);
}

@freezed
class Pagination with _$Pagination {
  const factory Pagination({
    required int page,
    required int size,
    required int totalElements,
    required int totalPages,
    required bool hasNext,
    required bool hasPrevious,
  }) = _Pagination;

  factory Pagination.fromJson(Map<String, dynamic> json) => _$PaginationFromJson(json);
}

@freezed
class PaginatedResponse<T> with _$PaginatedResponse<T> {
  const factory PaginatedResponse({
    required List<T> content,
    required Pagination pagination,
  }) = _PaginatedResponse;

  factory PaginatedResponse.fromJson(Map<String, dynamic> json, T Function(dynamic) fromJsonT) =>
      _$PaginatedResponseFromJson(json, fromJsonT);
}