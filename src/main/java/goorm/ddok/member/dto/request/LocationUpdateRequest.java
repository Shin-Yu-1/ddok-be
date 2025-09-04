package goorm.ddok.member.dto.request;


import goorm.ddok.global.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "LocationUpdateRequest", description = "주 활동 지역 수정 요청")
public class LocationUpdateRequest {
    LocationDto location;
}
