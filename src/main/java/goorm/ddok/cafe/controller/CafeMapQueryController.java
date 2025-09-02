package goorm.ddok.cafe.controller;

import goorm.ddok.cafe.dto.response.CafeMapItemResponse;
import goorm.ddok.cafe.service.CafeMapQueryService;
import goorm.ddok.global.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Cafe Map", description = "지도 카페 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/cafes")
public class CafeMapQueryController {

    private final CafeMapQueryService service;

    @Operation(
            summary = "카페 전체 조회(지도 범위)",
            description = "지도 영역(bounding box) 내의 카페를 조회합니다. deletedAt IS NULL 만 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponseDto.class),
                    examples = @ExampleObject(name = "성공 예시",
                            value = """
                {
                  "status": 200,
                  "message": "지도 카페 조회에 성공하였습니다.",
                  "data": [
                    {
                      "category": "cafe",
                      "cafeId": 1,
                      "title": "구지라지 카페",
                      "location": {
                        "latitude": 37.5665,
                        "longitude": 126.978,
                        "address": "서울특별시 강남구 테헤란로 123 (06234)"
                      }
                    },
                    {
                      "category": "cafe",
                      "cafeId": 2,
                      "title": "코딩 카페",
                      "location": {
                        "latitude": 37.565,
                        "longitude": 126.982,
                        "address": "서울특별시 중구 세종대로 110"
                      }
                    }
                  ]
                }
                """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 경계값",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponseDto.class),
                    examples = @ExampleObject(name = "경계 오류 예시",
                            value = """
                {
                  "status": 400,
                  "message": "잘못된 지도 경계값입니다.",
                  "data": null
                }
                """)
            )
    )
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<CafeMapItemResponse>>> getCafes(
            @Parameter(description = "남서쪽 위도", example = "37.55") @RequestParam BigDecimal swLat,
            @Parameter(description = "남서쪽 경도", example = "126.97") @RequestParam BigDecimal swLng,
            @Parameter(description = "북동쪽 위도", example = "37.58") @RequestParam BigDecimal neLat,
            @Parameter(description = "북동쪽 경도", example = "127.02") @RequestParam BigDecimal neLng,
            @Parameter(description = "중심 위도(선택)", example = "37.5665") @RequestParam(required = false) BigDecimal lat,
            @Parameter(description = "중심 경도(선택)", example = "126.978") @RequestParam(required = false) BigDecimal lng
    ) {
        List<CafeMapItemResponse> data = service.getCafesInBounds(swLat, swLng, neLat, neLng, lat, lng);
        return ResponseEntity.ok(ApiResponseDto.of(200,"지도 카페 조회에 성공하였습니다.", data));
    }
}