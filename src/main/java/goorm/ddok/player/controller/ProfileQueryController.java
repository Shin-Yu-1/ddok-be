package goorm.ddok.player.controller;


import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.player.dto.response.ProfileDetailResponse;
import goorm.ddok.player.service.ProfileQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 API")
public class ProfileQueryController {

    private final ProfileQueryService profileQueryService;

    /**
     * 프로필 상세 조회
     */
    @Operation(
            summary = "프로필 상세 조회",
            description = "특정 사용자의 프로필을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 200,
                  "message": "프로필 조회가 성공했습니다.",
                  "data": {
                    "userId": 1,
                    "isMine": true,
                    "isPublic": true,
                    "chatRoomId": null,
                    "dmRequestPending": false,
                    "nickname": "똑똑한 똑똑이",
                    "temperature": 36.6,
                    "ageGroup": "20대",
                    "profileImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0Q3QkRFMiIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjE1IiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiIGZvbnQtZmFtaWx5PSJJbnRlciI+7Ius7ZSEPC90ZXh0Pgo8L3N2Zz4K",
                    "mainPosition": "백엔드",
                    "subPositions": ["프론트엔드", "디자이너"],
                    "badges": [
                      { "type": "login","tier": "bronze" },
                      { "type": "complete","tier": "silver" },
                    ],
                    "abandonBadge": { "isGranted": true, "count": 5 },
                    "activeHours": { "start": "19", "end": "23" },
                    "traits": ["정리의 신", "실행력 갓", "내향인"],
                    "content": "Hi there, ~",
                    "portfolio": [
                      { "linkTitle": "깃헙", "link": "https://github.com/..." },
                      { "linkTitle": "블로그", "link": "https://blog..." }
                    ],
                    "location": {
                      "latitude": 37.5665,
                      "longitude": 126.9780,
                      "address": "강원특별자치도 삼척시 가곡면 가곡천로 249"
                    }
                  }
                }
                """))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 404, "message": "사용자를 찾을 수 없습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "400", description = "메인 포지션 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 400, "message": "사용자의 메인 포지션이 설정되지 않았습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "404", description = "온도 정보 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 404, "message": "사용자 온도 정보를 찾을 수 없습니다.", "data": null }
                """)))
    })
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponseDto<ProfileDetailResponse>> getProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long loginUserId = (userDetails != null) ? userDetails.getUser().getId() : null;
        ProfileDetailResponse response = profileQueryService.getProfile(userId, loginUserId);
        return ResponseEntity.ok(ApiResponseDto.of(200, "프로필 조회가 성공했습니다.", response));
    }
}