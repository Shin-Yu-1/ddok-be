package goorm.ddok.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "LocationDto", description = "오프라인 위치 정보")
public class LocationDto {
    @Schema(description = "위도", example = "37.5665")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "126.9780")
    private BigDecimal longitude;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로…")
    private String address;
}