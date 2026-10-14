package com.bikangu.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import org.springframework.data.domain.Page;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Meta meta
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Meta.builder().build());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, Meta.builder().build());
    }

    public static <T> ApiResponse<T> success(T data, Meta meta) {
        return new ApiResponse<>(true, data, null, meta);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, Meta.builder().build());
    }

    public static <T> ApiResponse<Page<T>> success(Page<T> page, Pagination pagination) {
        return new ApiResponse<>(true, page, null, Meta.builder().pagination(pagination).build());
    }

    public static <T> ApiResponse<Page<T>> success(Page<T> page, String message, Pagination pagination) {
        return new ApiResponse<>(true, page, message, Meta.builder().pagination(pagination).build());
    }

    public record Meta(
            Instant timestamp,
            String requestId,
            Pagination pagination
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Instant timestamp = Instant.now();
            private String requestId;
            private Pagination pagination;

            public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
            public Builder requestId(String requestId) { this.requestId = requestId; return this; }
            public Builder pagination(Pagination pagination) { this.pagination = pagination; return this; }

            public Meta build() {
                return new Meta(timestamp, requestId, pagination);
            }
        }
    }

    public record Pagination(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {
        public static Pagination of(int page, int size, long totalElements) {
            int totalPages = (int) Math.ceil((double) totalElements / size);
            return new Pagination(page, size, totalElements, totalPages, page < totalPages - 1, page > 0);
        }
    }
}