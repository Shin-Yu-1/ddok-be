package goorm.ddok.map.controller;

import goorm.ddok.cafe.dto.response.CafeMapItemResponse;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.map.dto.response.ProjectMapItemResponse;
import goorm.ddok.map.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Map", description = "지도 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
@Validated
public class MapController {

    private  final MapService mapService;

    @Operation(
            summary = "프로젝트 전체 조회(지도 범위)",
            description = """
                지도 영역(bounding box) 내의 카페를 조회합니다.
                - deletedAt IS NULL 만 반환합니다.
                - swLat ≤ neLat, swLng ≤ neLng 이어야 합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "status": 200,
                      "message": "지도 프로젝트 조회에 성공하였습니다.",
                      "data": [
                        {
                          "category": "project",
                          "projectId": 1,
                          "title": "구지라지 프로젝트",
                          "teamStatus": "RECRUITING",
                          "location": {
                            "address": "부산 해운대구 우동 센텀중앙로 90",
                            "region1depthName": "부산",
                            "region2depthName": "해운대구",
                            "region3depthName": "우동",
                            "roadName": "센텀중앙로",
                            "mainBuildingNo": "90",
                            "subBuildingNo": "",
                            "zoneNo": "48058",
                            "latitude": 35.1702,
                            "longitude": 129.1270
                          }
                        },
                        {
                          "category": "project",
                          "projectId": 2,
                          "title": "구라라지 프로젝트",
                          "teamStatus": "ONGOING",
                          "location": {
                            "address": "부산 해운대구 우동 센텀중앙로 90",
                            "region1depthName": "부산",
                            "region2depthName": "해운대구",
                            "region3depthName": "우동",
                            "roadName": "센텀중앙로",
                            "mainBuildingNo": "90",
                            "subBuildingNo": "",
                            "zoneNo": "48058",
                            "latitude": 35.1702,
                            "longitude": 129.1270
                          }
                        }
                      ]
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 경계값",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "경계 오류 예시", value = """
                    { "status": 400, "message": "잘못된 지도 경계값입니다.", "data": null }
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
    @GetMapping("/projects")
    public ResponseEntity<ApiResponseDto<List<ProjectMapItemResponse>>> getProjects(
            @Parameter(description = "남서쪽 위도", example = "37.55")
            @RequestParam @DecimalMin(value = "-90") @DecimalMax(value = "90") BigDecimal swLat,

            @Parameter(description = "남서쪽 경도", example = "126.97")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal swLng,

            @Parameter(description = "북동쪽 위도", example = "37.58")
            @RequestParam @DecimalMin(value = "-90") @DecimalMax(value = "90") BigDecimal neLat,

            @Parameter(description = "북동쪽 경도", example = "127.02")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal neLng,

            @Parameter(description = "중심 위도(선택)", example = "37.5665")
            @RequestParam(required = false) BigDecimal lat,

            @Parameter(description = "중심 경도(선택)", example = "126.978")
            @RequestParam(required = false) BigDecimal lng
    ) {
        List<ProjectMapItemResponse> data = mapService.getProjectsInBounds(swLat, swLng, neLat, neLng, lat, lng);
        return ResponseEntity.ok(ApiResponseDto.of(200, "지도 프로젝트 조회에 성공하였습니다.", data));
    }
}
