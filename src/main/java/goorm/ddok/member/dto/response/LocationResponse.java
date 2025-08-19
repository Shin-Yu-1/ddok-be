package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 위치 정보 응답 DTO")
public record LocationResponse(@Schema(description = "위도", example = "37.5665") Double latitude,
                               @Schema(description = "경도", example = "126.9780") Double longitude,
                               @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123") String address) {

}
