package goorm.ddok.map.controller;

import goorm.ddok.global.dto.PageResponse;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.map.dto.response.*;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            summary = "지도 전체 조회(지도 범위)",
            description = """
                지도 영역(bounding box) 내의 전체를 조회합니다.
                - swLat ≤ neLat, swLng ≤ neLng 이어야 합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "status": 200,
                      "message": "지도 전체 조회에 성공하였습니다.",
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
                          "category": "study",
                          "studyId": 1,
                          "title": "구지라지 스터디",
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
                          "category": "player",
                          "userId": 1,
                          "nickname": "멍한 백엔드",
                          "position": "백엔드",
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
                          },
                          "isMine": false
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
    @GetMapping("/all")
    public ResponseEntity<ApiResponseDto<List<AllMapItemResponse>>> getAllFlat(
            @Parameter(description = "남서쪽 위도", example = "37.55")
            @RequestParam @DecimalMin(value = "-90")  @DecimalMax(value = "90")  BigDecimal swLat,

            @Parameter(description = "남서쪽 경도", example = "126.97")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal swLng,

            @Parameter(description = "북동쪽 위도", example = "37.58")
            @RequestParam @DecimalMin(value = "-90")  @DecimalMax(value = "90")  BigDecimal neLat,

            @Parameter(description = "북동쪽 경도", example = "127.02")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal neLng,

            @Parameter(description = "중심 위도(선택)", example = "37.5665")
            @RequestParam(required = false) BigDecimal lat,

            @Parameter(description = "중심 경도(선택)", example = "126.978")
            @RequestParam(required = false) BigDecimal lng,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userId = (userDetails != null) ? userDetails.getId() : null;
        List<AllMapItemResponse> data =
                mapService.getAllInBounds(swLat, swLng, neLat, neLng, lat, lng, userId);

        return ResponseEntity.ok(ApiResponseDto.of(200, "지도 전체 조회에 성공하였습니다.", data));
    }

    @Operation(
            summary = "프로젝트 전체 조회(지도 범위)",
            description = """
                지도 영역(bounding box) 내의 프로젝트를 조회합니다.
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


    @Operation(
            summary = "스터디 전체 조회(지도 범위)",
            description = """
                지도 영역(bounding box) 내의 스터디를 조회합니다.
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
                      "message": "지도 스터디 조회에 성공하였습니다.",
                      "data": [
                        {
                          "category": "study",
                          "studyId": 1,
                          "title": "구지라지 스터디",
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
                          "category": "study",
                          "studyId": 2,
                          "title": "구라라지 스터디",
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
    @GetMapping("/studies")
    public ResponseEntity<ApiResponseDto<List<StudyMapItemResponse>>> getStudies(
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
        List<StudyMapItemResponse> data = mapService.getStudiesInBounds(swLat, swLng, neLat, neLng, lat, lng);
        return ResponseEntity.ok(ApiResponseDto.of(200, "지도 스터디 조회에 성공하였습니다.", data));
    }


    @Operation(
            summary = "플레이어 전체 조회(지도 범위)",
            description = """
                지도 영역(bounding box) 내의 플레이어를 조회합니다.
                - Public 플레이어만 반환합니다.
                - swLat ≤ neLat, swLng ≤ neLng 이어야 합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "status": 200,
                      "message": "지도 플레이어 조회에 성공하였습니다.",
                      "data": [
                        {
                          "category": "player",
                          "userId": 1,
                          "nickname": "멍한 백엔드",
                          "position": "백엔드",
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
                          },
                          "isMine": false
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
    @GetMapping("/players")
    public ResponseEntity<ApiResponseDto<List<PlayerMapItemResponse>>> getPlayers(
            @Parameter(description = "남서쪽 위도", example = "37.55")
            @RequestParam @DecimalMin(value = "-90")  @DecimalMax(value = "90")  BigDecimal swLat,

            @Parameter(description = "남서쪽 경도", example = "126.97")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal swLng,

            @Parameter(description = "북동쪽 위도", example = "37.58")
            @RequestParam @DecimalMin(value = "-90")  @DecimalMax(value = "90")  BigDecimal neLat,

            @Parameter(description = "북동쪽 경도", example = "127.02")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal neLng,

            @Parameter(description = "중심 위도(선택)", example = "37.5665")
            @RequestParam(required = false) BigDecimal lat,

            @Parameter(description = "중심 경도(선택)", example = "126.978")
            @RequestParam(required = false) BigDecimal lng,

            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {

        Long userId = (userDetails != null) ? userDetails.getId() : null;
        List<PlayerMapItemResponse> data = mapService.getPlayersInBounds(swLat, swLng, neLat, neLng, lat, lng, userId);
        return ResponseEntity.ok(ApiResponseDto.of(200, "지도 플레이어 조회에 성공하였습니다.", data));

    }

    @Operation(
            summary = "지도 통합 검색",
            description = """
            키워드(q)로 프로젝트/스터디/플레이어를 통합 검색합니다.
            - 카테고리 필터(categories): project|study|player (콤마 구분)
            - 바운딩 박스(swLat, swLng, neLat, neLng) 전달 시, 해당 영역 내에서만 검색
            - 중심 좌표(lat, lng) 전달 시, 결과를 거리순 정렬
            - 토큰이 있으면 player.isMine 계산에 사용
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "status": 200,
                      "message": "지도 스터디 조회에 성공하였습니다.",
                      "data": {
                        "pagination": {
                                          "currentPage": 0,
                                          "pageSize": 20,
                                          "totalPages": 1,
                                          "totalItems": 4
                        },
                        "items": [
                            {
                                "category": "study",
                                "studyId": 5,
                                "title": "SQL 문제풀이 스터디",
                                "teamStatus": "RECRUITING",
                                "bannerImageUrl": "https://my-shop-image-bucket.s3.ap-northeast-2.amazonaws.com/Banner/cd911832-221a-4367-bfe3-3dafeb6a2101_deco1-black.webp",
                                "location": {
                                    "address": "서울 종로구 청운효자동 세종대로 175",
                                    "region1depthName": "서울",
                                    "region2depthName": "종로구",
                                    "region3depthName": "청운효자동",
                                    "roadName": "세종대로",
                                    "mainBuildingNo": "175",
                                    "subBuildingNo": "",
                                    "zoneNo": "03027",
                                    "latitude": 37.575000,
                                    "longitude": 126.980000
                                }
                            },
                            {
                                "category": "project",
                                "projectId": 15,
                                "title": "한판교 플랫폼 MVP",
                                "teamStatus": "RECRUITING",
                                "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0Q1RjVFMyIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7tlZztjJDqtZAg7ZSM656r7Y+8IE1WUDwvdGV4dD4KPC9zdmc+Cg==",
                                "location": {
                                    "address": "서울 종로구 청운효자동 세종대로 175",
                                    "region1depthName": "서울",
                                    "region2depthName": "종로구",
                                    "region3depthName": "청운효자동",
                                    "roadName": "세종대로",
                                    "mainBuildingNo": "175",
                                    "subBuildingNo": "",
                                    "zoneNo": "03027",
                                    "latitude": 37.575000,
                                    "longitude": 126.980000
                                }
                            },
                            {
                                "category": "player",
                                "userId": 4,
                                "nickname": "까칠한 백엔드",
                                "profileImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0ZGREFCOSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjE1IiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiIGZvbnQtZmFtaWx5PSJJbnRlciI+6rmM67CxPC90ZXh0Pgo8L3N2Zz4K",
                                "temperature": 36.5,
                                "mainBadge": {
                                    "type": "login",
                                    "tier": "bronze"
                                },
                                "abandonBadge": {
                                    "count": 5,
                                    "isGranted": true
                                },
                                "location": {
                                    "address": "서울 종로구 청운효자동 세종대로 175",
                                    "region1depthName": "서울",
                                    "region2depthName": "종로구",
                                    "region3depthName": "청운효자동",
                                    "roadName": "세종대로",
                                    "mainBuildingNo": "175",
                                    "subBuildingNo": "",
                                    "zoneNo": "03027",
                                    "latitude": 37.575000,
                                    "longitude": 126.980000
                                },
                                "isMine": false
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
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<PageResponse<AllMapItemSearchResponse>>> search(
            @Parameter(description = "검색 키워드", example = "구지라지")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "남서쪽 위도", example = "37.55")
            @RequestParam @DecimalMin(value = "-90")  @DecimalMax(value = "90")  BigDecimal swLat,

            @Parameter(description = "남서쪽 경도", example = "126.97")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal swLng,

            @Parameter(description = "북동쪽 위도", example = "37.58")
            @RequestParam @DecimalMin(value = "-90")  @DecimalMax(value = "90")  BigDecimal neLat,

            @Parameter(description = "북동쪽 경도", example = "127.02")
            @RequestParam @DecimalMin(value = "-180") @DecimalMax(value = "180") BigDecimal neLng,

            @Parameter(description = "중심 위도(선택, 거리순 정렬)", example = "35.1702")
            @RequestParam(required = false) BigDecimal lat,

            @Parameter(description = "중심 경도(선택, 거리순 정렬)", example = "129.1270")
            @RequestParam(required = false) BigDecimal lng,

            @Parameter(description = "검색 카테고리(project,study,player,cafe)", example = "project,study,player,cafe")
            @RequestParam(required = false) String category,

            @Parameter(description = "project,study 용 RECRUITING,ONGOING", example = "RECRUITING,ONGOING")
            @RequestParam(required = false) String filter,

            @Parameter(description = "페이지(0-base)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "4") @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;

        PageResponse<AllMapItemSearchResponse> data = mapService.search(
                keyword, swLat, swLng, neLat, neLng, lat, lng,
                category, userId, page, size, filter
        );

        return ResponseEntity.ok(ApiResponseDto.of(200, "지도 검색에 성공하였습니다.", data));
    }
}
