package goorm.ddok.cafe.controller;

import goorm.ddok.cafe.dto.response.CafeReviewListResponse;
import goorm.ddok.cafe.service.CafeReviewQueryService;
import goorm.ddok.global.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cafe Reviews", description = "카페 후기 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/cafes")
public class CafeReviewQueryController {

    private final CafeReviewQueryService service;

    @Operation(
            summary = "카페 후기 상세 리스트 조회",
            description = """
                카페의 활성 후기(soft delete 제외)를 페이지네이션으로 조회합니다.
                - 기본 정렬: 작성일 최신순
                - page는 0부터 시작
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "status": 200,
                      "message": "카페 후기 리스트 조회에 성공하였습니다.",
                      "data": {
                        "cafeId": 1,
                        "title": "구지라지 카페",
                        "pagination": {
                          "currentPage": 0,
                          "pageSize": 4,
                          "totalPages": 1,
                          "totalItems": 2
                        },
                        "cafeReviews": [
                          {
                            "userId": 1,
                            "nickname": "고라니",
                            "profileImageUrl": "https://img.cdn/profiles/1.png",
                            "rating": 4.5,
                            "cafeReviewTag": ["분위기가 좋아요", "조용해요"],
                            "createdAt": "2025-08-14T10:12:30",
                            "updatedAt": "2025-08-22T09:05:11"
                          }
                        ]
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "404", description = "카페 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "존재하지 않는 카페입니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 500, "message": "서버 내부 오류", "data": null }
                    """)))
    })
    @GetMapping("/{cafeId}/reviews")
    public ResponseEntity<ApiResponseDto<CafeReviewListResponse>> getReviews(
            @Parameter(description = "카페 ID", example = "1") @PathVariable Long cafeId,
            @Parameter(description = "페이지(0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        CafeReviewListResponse data = service.getCafeReviews(cafeId, page, size);
        return ResponseEntity.ok(ApiResponseDto.of(200, "카페 후기 리스트 조회에 성공하였습니다.", data));
    }
}