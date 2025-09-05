package goorm.ddok.map.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.map.dto.response.PinOverlayResponse;
import goorm.ddok.map.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Map", description = "지도 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapOverlayController {

    private final MapService mapService;

    @Operation(
            summary = "핀 오버레이 정보 조회",
            description = """
            category: project|study|cafe|player
            - 각 카테고리별로 필드가 다릅니다.
            - 인증 사용 시 player.isMine 계산에 활용됩니다.
            """
    )
    @GetMapping("/overlay/{category}/{id}")
    public ResponseEntity<ApiResponseDto<PinOverlayResponse>> getOverlay(
            @Parameter(description = "카테고리", example = "project") @PathVariable String category,
            @Parameter(description = "리소스 ID", example = "1")   @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = (userDetails != null) ? userDetails.getId() : null;
        PinOverlayResponse data = mapService.getOverlay(category, id, currentUserId);
        return ResponseEntity.ok(ApiResponseDto.of(200, "핀 오버레이 정보 조회에 성공하였습니다.", data));
    }
}
