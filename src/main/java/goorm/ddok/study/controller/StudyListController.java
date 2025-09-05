package goorm.ddok.study.controller;

import goorm.ddok.global.dto.PageResponse;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.study.dto.response.StudyListResponse;
import goorm.ddok.study.service.StudyListService;
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
@RequestMapping("/api/studies")
@RequiredArgsConstructor
@Tag(name = "Study", description = "스터디 API")
public class StudyListController {

    private final StudyListService studyListService;

    @Operation(
            summary = "스터디 리스트 조회",
            description = """
            스터디 리스트를 페이지네이션으로 조회합니다.
            - 최신 등록순(createdAt DESC)
            - 주소는 오프라인의 경우 "광역시/도 + 시/군/구"(예: 서울 강남구), 온라인이면 "online"
            - teamStatus: CLOSED → ONGOING, null → ONGOING
            """
    )
    @ApiResponse(responseCode = "200", description = "스터디 리스트 조회 성공",
            content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                    examples = @ExampleObject(name="성공 예시", value = """
            {
                 "status": 200,
                 "message": "스터디 리스트 조회가 성공했습니다.",
                 "data": {
                     "pagination": {
                         "currentPage": 0,
                         "pageSize": 20,
                         "totalPages": 1,
                         "totalItems": 2
                     },
                     "items": [
                         {
                             "studyId": 2,
                             "title": "스프링 시큐리티 심화 스터디",
                             "teamStatus": "RECRUITING",
                             "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0Y1Q0JBNyIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7siqTtlITrp4Eg7Iuc7YGQ66as7YuwIOyLrO2ZlCDsiqTthLDrlJQ8L3RleHQ+Cjwvc3ZnPgo=",
                             "capacity": 5,
                             "mode": "offline",
                             "address": "전북 익산시",
                             "studyType": "취업/면접",
                             "preferredAges": {
                                 "ageMin": 20,
                                 "ageMax": 40
                             },
                             "expectedMonth": 2,
                             "startDate": "2025-10-15"
                         },
                         {
                             "studyId": 1,
                             "title": "스프링 시큐리티 심화 스터디",
                             "teamStatus": "CLOSED",
                             "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0Y1Q0JBNyIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7siqTtlITrp4Eg7Iuc7YGQ66as7YuwIOyLrO2ZlCDsiqTthLDrlJQ8L3RleHQ+Cjwvc3ZnPgo=",
                             "capacity": 5,
                             "mode": "offline",
                             "address": "전북 익산시",
                             "studyType": "취업/면접",
                             "preferredAges": {
                                 "ageMin": 20,
                                 "ageMax": 40
                             },
                             "expectedMonth": 2,
                             "startDate": "2025-10-15"
                         }
                     ]
                 }
             }
            """)))
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponse<StudyListResponse>>> list(
            @Parameter(description = "페이지(0-base)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "4")
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<StudyListResponse> result = studyListService.getStudies(page, size);
        return ResponseEntity.ok(
                ApiResponseDto.of(200, "스터디 리스트 조회가 성공했습니다.", PageResponse.of(result))
        );
    }
}
