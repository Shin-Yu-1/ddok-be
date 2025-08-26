package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "LocationRequest",
        description = "위치 정보 요청 DTO",
        requiredProperties = {"latitude", "longitude", "address"},
        example = """
    {
      "latitude": 37.5665,
      "longitude": 126.9780,
      "address": "서울특별시 강남구 테헤란로 123"
    }
    """
)
public class LocationRequest {

    @Schema(
            description = "위도",
            example = "37.5665",
            minimum = "-90.0",
            maximum = "90.0",
            type = "number",
            format = "double",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "위도는 필수 입력 값입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90.0 이하이어야 합니다.")
    private Double latitude;

    @Schema(
            description = "경도",
            example = "126.9780",
            minimum = "-180.0",
            maximum = "180.0",
            type = "number",
            format = "double",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "경도는 필수 입력 값입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180.0 이하이어야 합니다.")
    private Double longitude;

    @Schema(
            description = "주소",
            example = "서울특별시 강남구 테헤란로 123",
            type = "string",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "주소는 필수 입력 값입니다.")
    private String address;
}
