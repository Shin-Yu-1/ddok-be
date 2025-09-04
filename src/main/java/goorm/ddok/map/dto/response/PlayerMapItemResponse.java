package goorm.ddok.map.dto.response;


import goorm.ddok.global.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "지도용 플레이어 항목")
public class PlayerMapItemResponse {

    @Schema(description = "카테고리", example = "player")
    private String category;

    @Schema(description = "플레이어 ID", example = "1")
    private Long userId;

    @Schema(description = "플레이어 닉네임", example = "똑똑한 백엔드")
    private String nickname;

    @Schema(description = "플레이어 포지션", example = "백엔드")
    private String position;

    @Schema(description = "내 정보인지 여부", example = "true")
    private boolean IsMine;

    @Schema(name = "LocationDto", description = "카카오 road_address 매핑 DTO")
    private LocationDto location;
}
