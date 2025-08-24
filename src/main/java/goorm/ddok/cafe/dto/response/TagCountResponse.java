package goorm.ddok.cafe.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "태그별 카운트")
public record TagCountResponse(
        @Schema(description = "태그명", example = "분위기가 좋아요")
        String tagName,
        @Schema(description = "태그 카운트", example = "32")
        long tagCount
) {}