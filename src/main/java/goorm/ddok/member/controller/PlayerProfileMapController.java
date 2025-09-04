package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.member.dto.response.PlayerProfileMapItemResponse;
import goorm.ddok.member.service.PlayerProfileMapService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 API")
public class PlayerProfileMapController {

    private final PlayerProfileMapService playerProfileMapService;

    @GetMapping("/{userId}/profile/map")
    public ResponseEntity<ApiResponseDto<List<PlayerProfileMapItemResponse>>> getProfileMap(
            @PathVariable Long userId,

            @Parameter(description = "남서쪽 위도", example = "37.55")
            @RequestParam @DecimalMin(value = "-90")  @DecimalMax(value = "90") BigDecimal swLat,

            @Parameter(description = "남서쪽 경도", example = "126.97")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal swLng,

            @Parameter(description = "북동쪽 위도", example = "37.58")
            @RequestParam @DecimalMin(value = "-90")  @DecimalMax(value = "90")  BigDecimal neLat,

            @Parameter(description = "북동쪽 경도", example = "127.02")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal neLng,

            @Parameter(description = "중심 위도(선택)", example = "37.5665")
            @RequestParam(required = false) BigDecimal lat,

            @Parameter(description = "중심 경도(선택)", example = "126.978")
            @RequestParam(required = false) BigDecimal lng
    ) {
        List<PlayerProfileMapItemResponse> data =
                playerProfileMapService.getProfileMapInBounds(swLat, swLng, neLat, neLng, lat, lng, userId);

        return ResponseEntity.ok(ApiResponseDto.of(200, "프로필 지도 조회에 성공하였습니다.", data));
    }
}
