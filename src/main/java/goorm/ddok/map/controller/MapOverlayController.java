package goorm.ddok.map.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.map.dto.response.PinOverlayResponse;
import goorm.ddok.map.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                        "status": 200,
                        "message": "핀 오버레이 정보 조회에 성공하였습니다.",
                        "data": {
                            "category": "player",
                            "address": "전북 익산시",
                            "userId": 1,
                            "nickname": "요망한 백엔드",
                            "profileImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0Y1Q0JBNyIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjE1IiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiIGZvbnQtZmFtaWx5PSJJbnRlciI+7JqU67CxPC90ZXh0Pgo8L3N2Zz4K",
                            "mainPosition": "백엔드",
                            "temperature": 36.5,
                            "isMine": true,
                            "mainBadge": {
                                "type": "login",
                                "tier": "bronze"
                            },
                            "abandonBadge": {
                                "count": 5,
                                "isGranted": true
                            },
                            "latestProject": {
                                "id": 1,
                                "title": "청년 창업 지원 포털",
                                "teamStatus": "RECRUITING"
                            },
                            "latestStudy": {
                                "id": 1,
                                "title": "스프링 시큐리티 심화 스터디",
                                "teamStatus": "RECRUITING"
                            }
                        }
                    },
                    {
                        "status": 200,
                        "message": "핀 오버레이 정보 조회에 성공하였습니다.",
                        "data": {
                            "category": "project",
                            "address": "전북 익산시",
                            "projectId": 1,
                            "title": "청년 창업 지원 포털",
                            "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0ZGRTU5OSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7ssq3rhYQg7LC97JeFIOyngOybkCDtj6zthLg8L3RleHQ+Cjwvc3ZnPgo=",
                            "teamStatus": "RECRUITING",
                            "capacity": 7,
                            "mode": "offline",
                            "preferredAges": {
                                "ageMin": 20,
                                "ageMax": 40
                            },
                            "expectedMonth": 6,
                            "startDate": "2025-11-03",
                            "positions": [
                                "백엔드",
                                "프론트엔드",
                                "디자이너",
                                "PM"
                            ]
                        }
                    },
                    {
                        "status": 200,
                        "message": "핀 오버레이 정보 조회에 성공하였습니다.",
                        "data": {
                            "category": "study",
                            "address": "전북 익산시",
                            "studyId": 1,
                            "title": "스프링 시큐리티 심화 스터디",
                            "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0Y1Q0JBNyIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7siqTtlITrp4Eg7Iuc7YGQ66as7YuwIOyLrO2ZlCDsiqTthLDrlJQ8L3RleHQ+Cjwvc3ZnPgo=",
                            "teamStatus": "RECRUITING",
                            "mode": "offline",
                            "preferredAges": {
                                "ageMin": 20,
                                "ageMax": 40
                            },
                            "expectedMonth": 2,
                            "startDate": "2025-10-15",
                            "studyType": "취업/면접"
                        }
                    },
                    {
                        "status": 200,
                        "message": "핀 오버레이 정보 조회에 성공하였습니다.",
                        "data": {
                            "category": "cafe",
                            "address": "서울 강남구",
                            "cafeId": 1,
                            "title": "룸카페 바니",
                            "bannerImageUrl": "",
                            "rating": 2,
                            "reviewCount": 4
                        }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 카테고리",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "지원하지 않는 카테고리", value = """
                    { "status": 400, "message": "지원하지 않는 카테고리입니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "400", description = "필수 파라미터 누락",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "경계 오류 예시", value = """
                    { "status": 400, "message": "필수 파라미터가 누락되었습니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 500, "message": "서버 내부 오류", "data": null }
                    """)))
    })
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
