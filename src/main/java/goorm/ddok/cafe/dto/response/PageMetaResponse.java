package goorm.ddok.cafe.dto.response;

public record PageMetaResponse(
        int currentPage,
        int pageSize,
        int totalPages,
        long totalItems
) {}