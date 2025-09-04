package goorm.ddok.cafe.dto.response;

import goorm.ddok.global.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "지도용 카페 항목")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CafeMapItemResponse {

    @Schema(description = "카테고리", example = "cafe")
    private String category;

    @Schema(description = "카페 ID", example = "1")
    private Long cafeId;

    @Schema(description = "카페명", example = "구지라지 카페")
    private String title;

    @Schema(description = "카페 좌표/주소 정보")
    private LocationDto location;

}

