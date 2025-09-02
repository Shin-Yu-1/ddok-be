package goorm.ddok.cafe.controller;

import goorm.ddok.cafe.dto.response.CafeStatsResponse;
import goorm.ddok.cafe.service.CafeStatsService;
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

@Tag(name = "Cafe Reviews", description = "카페 후기 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/cafes")
public class CafeStatsController {

    private final CafeStatsService cafeStatsService;

    @Operation(
            summary = "카페 후기 통계 조회",
            description = "지정한 카페의 활성(review.status=ACTIVE, deletedAt IS NULL) 후기 통계를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "status": 200,
                      "message": "카페 후기 통계 조회에 성공하였습니다.",
                      "data": {
                        "cafeId": 1,
                        "title": "구지라지 카페",
                        "reviewCount": 193,
                        "cafeReviewTag": [
                          {"tagName":"분위기가 좋아요","tagCount":32},
                          {"tagName":"조용해요","tagCount":25}
                        ],
                        "totalRating": 3.9
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
    @GetMapping("/{cafeId}/stats")
    public ResponseEntity<ApiResponseDto<CafeStatsResponse>> getStats(
            @Parameter(description = "카페 ID", example = "1") @PathVariable Long cafeId
    ) {
        CafeStatsResponse data = cafeStatsService.getCafeStats(cafeId);
        return ResponseEntity.ok(ApiResponseDto.of(200, "카페 후기 통계 조회에 성공하였습니다.", data));
    }
}