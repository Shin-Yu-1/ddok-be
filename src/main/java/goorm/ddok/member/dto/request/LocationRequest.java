package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

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
            format = "BigDecimal",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "위도는 필수 입력 값입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90.0 이하이어야 합니다.")
    private BigDecimal latitude;

    @Schema(
            description = "경도",
            example = "126.9780",
            minimum = "-180.0",
            maximum = "180.0",
            type = "number",
            format = "BigDecimal",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "경도는 필수 입력 값입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180.0 이하이어야 합니다.")
    private BigDecimal longitude;

    @Schema(
            description = "주소",
            example = "서울특별시 강남구 테헤란로 123",
            type = "string",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "주소는 필수 입력 값입니다.")
    private String address;

    @Schema(description = "광역시/도 (region_1depth_name)",
            example = "전북")
    private String region1depthName;

    @Schema(description = "시/군/구 (region_2depth_name)",
            example = "익산시")
    private String region2depthName;

    @Schema(description = "동/읍/면 (region_3depth_name)",
            example = "부송동")
    private String region3depthName;

    @Schema(description = "도로명 (road_name)",
            example = "망산길")
    private String roadName;

    @Schema(description = "건물 본번 (main_building_no)",
            example = "11")
    private String mainBuildingNo;

    @Schema(description = "건물 부번 (sub_building_no)",
            example = "17")
    private String subBuildingNo;

    @Schema(description = "우편번호(필요 시) (zone_no)",
            example = "54547")
    private String zoneNo;
}
