package com.decisionhub.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Generic DTO wrapper representing a paginated response, decoupling Spring Data's Page from the API.
 */
public record PageResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean last
) {
    /**
     * Map a Spring Data Page to the PageResponse wrapper.
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
