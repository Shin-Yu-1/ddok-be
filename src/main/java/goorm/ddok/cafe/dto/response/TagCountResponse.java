package goorm.ddok.cafe.dto.response;

public record TagCountResponse(
        String tagName,
        long tagCount
) {}