package goorm.ddok.player.controller;

import goorm.ddok.global.dto.PageResponse;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.player.dto.response.ProfileSearchResponse;
import goorm.ddok.player.service.ProfileSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Player", description = "플레이어 검색 API")
public class ProfileSearchController {

    private final ProfileSearchService profileSearchService;

    @Operation(
            summary = "플레이어 검색",
            description = """
                닉네임, 포지션, 주소를 기준으로 플레이어를 검색합니다.
                - 검색어는 콤마(,) 또는 공백( )으로 구분하여 여러 개 입력할 수 있습니다.
                - 검색어가 없으면 전체 공개 프로필을 페이지네이션으로 조회합니다.
                - 기본 정렬: 닉네임 오름차순(대소문자 구분 없음)
                - page는 0부터 시작
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "플레이어 검색 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
            examples = @ExampleObject(value = """
                        {
                            "status": 200,
                            "message": "플레이어 검색이 성공적으로 처리되었습니다.",
                            "data": {
                                "pagination": {
                                    "currentPage": 0,
                                    "pageSize": 10,
                                    "totalPages": 1,
                                    "totalItems": 1
                                },
                                "items": [
                                    {
                                        "userId": 1,
                                        "category": "players",
                                        "nickname": "요망한 백엔드",
                                        "profileImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0Y1Q0JBNyIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjE1IiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiIGZvbnQtZmFtaWx5PSJJbnRlciI+7JqU67CxPC90ZXh0Pgo8L3N2Zz4K",
                                        "mainBadge": {
                                            "type": "login",
                                            "tier": "bronze"
                                        },
                                        "abandonBadge": {
                                            "count": 5,
                                            "isGranted": true
                                        },
                                        "mainPosition": "백엔드",
                                        "address": "전북 익산시",
                                        "temperature": 36.5,
                                        "chatRoomId": null,
                                        "dmRequestPending": false,
                                        "isMine": false
                                    }
                                ]
                            }
                        }
                       """)))
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<PageResponse<ProfileSearchResponse>>> search(
            @Parameter(description = "검색어(닉네임/포지션/주소). 콤마/공백 구분", example = "똑똑이,강남구,백엔드")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "페이지(0-base)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long currentUserId = (userDetails != null) ? userDetails.getId() : null;

        Page<ProfileSearchResponse> result =
                profileSearchService.searchPlayers(keyword, page, size, currentUserId);

        return ResponseEntity.ok(
                ApiResponseDto.of(200, "플레이어 검색이 성공적으로 처리되었습니다.", PageResponse.of(result))
        );
    }
}
