package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 위치 정보")
public record LocationResponse(@Schema(example = "37.5665") Double latitude,
                               @Schema(example = "126.9780") Double longitude,
                               @Schema(example = "서울특별시 강남구 테헤란로…") String address) {

}
