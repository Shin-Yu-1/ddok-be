package goorm.ddok.cafe.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "카페 좌표/주소 정보")
public record CafeLocationResponse(
        @Schema(description = "위도", example = "37.5665")
        BigDecimal latitude,
        @Schema(description = "경도", example = "126.978")
        BigDecimal longitude,
        @Schema(description = "표시용 주소(도로명 또는 행정동)", example = "서울특별시 중구 세종대로 110")
        String address
) {}