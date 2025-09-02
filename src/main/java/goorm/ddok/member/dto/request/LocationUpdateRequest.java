package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(name = "LocationUpdateRequest", description = "주 활동 지역 수정 요청")
public class LocationUpdateRequest {

    @Valid
    @NotNull
    @Schema(description = "위치 블록", requiredMode = Schema.RequiredMode.REQUIRED)
    private Location location;

    @Getter @Setter
    @Schema(name = "LocationUpdateRequest.Location", description = "위치 좌표/주소", example = """
        {
          "latitude": 37.5665,
          "longitude": 126.9780,
          "address": "서울특별시 강남구 테헤란로 123"
        }
        """)
    public static class Location {
        @NotNull @Schema(description = "위도", example = "37.5665")
        private Double latitude;
        @NotNull @Schema(description = "경도", example = "126.9780")
        private Double longitude;
        @Schema(description = "주소(선택)", example = "서울특별시 강남구 테헤란로 123")
        private String address; // 필요 시 카카오 세부 필드로 확장
    }
}