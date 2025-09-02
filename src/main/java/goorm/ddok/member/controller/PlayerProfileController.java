package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.dto.ProfileDto;
import goorm.ddok.member.dto.request.*;
import goorm.ddok.member.service.PlayerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/players/profile")
@RequiredArgsConstructor
@Validated
public class PlayerProfileController {

    private final PlayerProfileService service;

    @PatchMapping("/positions")
    @Operation(
            summary = "포지션 수정",
            description = """
                내 포지션을 수정합니다.
                - mainPosition은 필수
                - subPositions는 최대 2개, mainPosition과 중복 불가
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name="성공 예시", value = """
                    {
                      "status": 200,
                      "message": "요청이 성공적으로 처리되었습니다.",
                      "data": {
                        "profile": {
                          "userId": 10,
                          "isMine": true,
                          "chatRoomId": null,
                          "dmRequestPending": false,
                          "isPublic": true,
                          "profileImageUrl": "https://cdn.example.com/user/10.png",
                          "nickname": "고라니",
                          "temperature": 36.5,
                          "ageGroup": null,
                          "mainPosition": "백엔드",
                          "subPositions": ["프론트엔드","디자이너"],
                          "mainBadge": null,
                          "abandonBadge": null,
                          "activeHours": { "start": 9, "end": 18 },
                          "traits": ["성실","실행력"],
                          "content": null,
                          "portfolio": null,
                          "location": { "latitude": 37.5665, "longitude": 126.978, "address": "서울 강남구" },
                          "techStacks": ["Java","Spring","JPA"]
                        }
                      }
                    }"""))),
            @ApiResponse(responseCode = "400", description = "검증 실패(메인 필수/서브 2개 초과/중복 등)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name="메인 없음", value = """
                                    { "status": 400, "message": "메인 포지션은 필수입니다.", "data": null }"""),
                                    @ExampleObject(name="서브 초과", value = """
                                    { "status": 400, "message": "서브 포지션은 최대 2개까지 가능합니다.", "data": null }"""),
                                    @ExampleObject(name="중복", value = """
                                    { "status": 400, "message": "메인/서브 포지션이 중복됩니다.", "data": null }""")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }"""))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "사용자를 찾을 수 없습니다.", "data": null }""")))
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updatePositions(
            @Valid @RequestBody PositionsUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updatePositions(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/traits")
    @Operation(summary = "나의 성향 수정", description = "나의 성향(traits)을 전체 치환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 200, "message": "요청이 성공적으로 처리되었습니다.", "data": { "profile": { /* 생략 */ } } }"""))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }"""))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "사용자를 찾을 수 없습니다.", "data": null }""")))
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateTraits(
            @Valid @RequestBody TraitsUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updateTraits(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/hours")
    @Operation(
            summary = "주 활동 시간 수정",
            description = "하루 기준 활동 시작/종료 시간을 0~24 범위의 정수로 설정합니다. 종료시간은 시작시간 이상이어야 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 200, "message": "요청이 성공적으로 처리되었습니다.", "data": { "profile": { /* 생략 */ } } }"""))),
            @ApiResponse(responseCode = "400", description = "형식/범위 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name="형식오류", value = """
                                    { "status": 400, "message": "활동 시간 형식이 올바르지 않습니다.", "data": null }"""),
                                    @ExampleObject(name="범위오류", value = """
                                    { "status": 400, "message": "종료 시간은 시작 시간 이상이어야 합니다.", "data": null }""")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }"""))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "사용자를 찾을 수 없습니다.", "data": null }""")))
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateActiveHours(
            @Valid @RequestBody ActiveHoursRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updateActiveHours(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/location")
    @Operation(
            summary = "주 활동 지역 수정",
            description = """
                위/경도와 주소를 저장합니다.
                - latitude/longitude 필수
                - address는 선택
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 200, "message": "요청이 성공적으로 처리되었습니다.", "data": { "profile": { /* 생략 */ } } }"""))),
            @ApiResponse(responseCode = "400", description = "위치 정보 누락",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 400, "message": "위치 정보가 올바르지 않습니다.", "data": null }"""))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }"""))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "사용자를 찾을 수 없습니다.", "data": null }""")))
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateLocation(
            @Valid @RequestBody LocationUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updateLocation(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/content")
    @Operation(summary = "자기 소개 수정", description = "자기소개 문구를 생성/수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 200, "message": "요청이 성공적으로 처리되었습니다.", "data": { "profile": { /* 생략 */ } } }"""))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }""")))
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> upsertContent(
            @Valid @RequestBody ContentUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.upsertContent(req, me); // 저장 엔티티 미정 → 응답만 포함(null)
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/portfolio")
    @Operation(summary = "포트폴리오 수정", description = "포트폴리오 링크 목록을 생성/수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 200, "message": "요청이 성공적으로 처리되었습니다.", "data": { "profile": { /* 생략 */ } } }"""))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }""")))
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> upsertPortfolio(
            @Valid @RequestBody PortfolioUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.upsertPortfolio(req, me); // 저장 엔티티 미정 → 응답만 포함(null)
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping
    @Operation(summary = "프로필 공개/비공개 설정", description = "프로필 공개 여부를 설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 200, "message": "요청이 성공적으로 처리되었습니다.", "data": { "profile": { /* 생략 */ } } }"""))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }""")))
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateVisibility(
            @RequestParam(name = "isPublic") boolean isPublic,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updateVisibility(isPublic, me); // 저장 엔티티 미정 → 응답만 포함(null)
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/stacks")
    @Operation(summary = "기술 스택 수정", description = "보유 기술 스택을 전체 치환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 200, "message": "요청이 성공적으로 처리되었습니다.", "data": null }"""))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }""")))
    })
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateStacks(
            @Valid @RequestBody TechStacksUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        service.updateTechStacks(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("data", null)));
    }
}