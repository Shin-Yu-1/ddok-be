package goorm.ddok.project.controller;


import goorm.ddok.global.dto.PageResponse;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.project.dto.response.ProjectListResponse;
import goorm.ddok.project.service.ProjectListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "프로젝트 API")
public class ProjectListController {

    private final ProjectListService projectListService;

    @Operation(
            summary = "프로젝트 리스트 조회",
            description = """
                프로젝트 목록을 페이지네이션으로 조회합니다.
                - 삭제되지 않은 공고만 반환
                - createdAt DESC 정렬
                - 주소는 오프라인: "{region1} {region2}", 온라인: "online"
                """)

    @ApiResponse(responseCode = "200", description = "프로젝트 리스트 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                    examples = @ExampleObject(name="성공 예시", value = """
            {
                "status": 200,
                "message": "프로젝트 리스트 조회가 성공했습니다.",
                "data": {
                    "pagination": {
                        "currentPage": 0,
                        "pageSize": 20,
                        "totalPages": 1,
                        "totalItems": 3
                    },
                    "items": [
                        {
                            "projectId": 3,
                            "title": "청년 창업 지원 포털",
                            "teamStatus": "RECRUITING",
                            "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0ZGRTU5OSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7ssq3rhYQg7LC97JeFIOyngOybkCDtj6zthLg8L3RleHQ+Cjwvc3ZnPgo=",
                            "positions": [
                                "PM",
                                "디자이너",
                                "백엔드",
                                "프론트엔드"
                            ],
                            "capacity": 7,
                            "mode": "online",
                            "address": "online",
                            "preferredAges": {
                                "ageMin": 20,
                                "ageMax": 40
                            },
                            "expectedMonth": 6,
                            "startDate": "2025-11-03"
                        },
                        {
                            "projectId": 2,
                            "title": "청년 창업 지원 포털",
                            "teamStatus": "RECRUITING",
                            "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0ZGRTU5OSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7ssq3rhYQg7LC97JeFIOyngOybkCDtj6zthLg8L3RleHQ+Cjwvc3ZnPgo=",
                            "positions": [
                                "PM",
                                "디자이너",
                                "백엔드",
                                "프론트엔드"
                            ],
                            "capacity": 7,
                            "mode": "offline",
                            "address": "전북 익산시",
                            "preferredAges": {
                                "ageMin": 20,
                                "ageMax": 40
                            },
                            "expectedMonth": 6,
                            "startDate": "2025-11-03"
                        },
                        {
                            "projectId": 1,
                            "title": "청년 창업 지원 포털",
                            "teamStatus": "ONGOING",
                            "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0ZGRTU5OSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7ssq3rhYQg7LC97JeFIOyngOybkCDtj6zthLg8L3RleHQ+Cjwvc3ZnPgo=",
                            "positions": [
                                "PM",
                                "디자이너",
                                "백엔드",
                                "프론트엔드"
                            ],
                            "capacity": 7,
                            "mode": "offline",
                            "address": "전북 익산시",
                            "preferredAges": {
                                "ageMin": 20,
                                "ageMax": 40
                            },
                            "expectedMonth": 6,
                            "startDate": "2025-11-03"
                        }
                    ]
                }
            }
            """)))
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponse<ProjectListResponse>>> getProjects(
            @Parameter(description = "페이지(0-base)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "4") @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProjectListResponse> result = projectListService.getProjects(page, size);
        return ResponseEntity.ok(
                ApiResponseDto.of(200, "프로젝트 리스트 조회가 성공했습니다.", PageResponse.of(result))
        );
    }
}
