package com.bikangu.common.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtil {

    private PaginationUtil() {}

    public static Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, sortBy));
    }

    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, "createdAt", "desc");
    }

    public static <T> com.bikangu.common.response.ApiResponse.Pagination toPagination(Page<T> page) {
        return com.bikangu.common.response.ApiResponse.Pagination.of(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}