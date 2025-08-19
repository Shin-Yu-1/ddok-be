package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "위치 정보 요청 DTO")
public class LocationRequest {

    @Schema(description = "위도", example = "37.5665")
    @NotNull(message = "위도는 필수 입력 값입니다.")
    @DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "위도는 90.0 이하이어야 합니다.")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
    @NotNull(message = "경도는 필수 입력 값입니다.")
    @DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "경도는 180.0 이하이어야 합니다.")
    private Double longitude;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    @NotBlank(message = "주소는 필수 입력 값입니다.")
    private String address;
}
