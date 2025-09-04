package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.dto.ProfileDto;
import goorm.ddok.member.dto.request.*;
import goorm.ddok.member.service.PlayerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Profile Settings", description = "프로필 정보변경 API")
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
            ,
    security = @SecurityRequirement(name = "Authorization"),
    parameters = {
        @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                description = "Bearer {accessToken}",
                examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
    }
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
    @Operation(summary = "나의 성향 수정", description = "나의 성향(traits)을 전체 치환합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
            })
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
            description = "하루 기준 활동 시작/종료 시간을 0~24 범위의 정수로 설정합니다. 종료시간은 시작시간 이상이어야 합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
            }
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



    @PatchMapping("/content")
    @Operation(summary = "자기 소개 수정", description = "자기소개 문구를 생성/수정합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 200, "message": "자기소개 수정에 성공했습니다.", "data": { "profile": { /* 생략 */ } } }"""))),
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
        return ResponseEntity.ok(ApiResponseDto.of(200, "자기소개 수정에 성공했습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/portfolio")
    @Operation(
            summary = "포트폴리오 생성/수정",
            description = "사용자의 포트폴리오 링크 목록을 **전체 치환**합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "message": "요청이 성공적으로 처리되었습니다.",
                      "data": {
                        "userId": 1,
                        "isMine": true,
                        "chatRoomId": null,
                        "dmRequestPending": false,
                        "isPublic": true,
                        "profileImageUrl": "",
                        "nickname": "똑똑한 똑똑이",
                        "temperature": 36.6,
                        "ageGroup": "20대",
                        "mainPosition": "백엔드",
                        "subPositions": ["프론트엔드", "데브옵스"],
                        "badges": [],
                        "abandonBadge": { "isGranted": false, "count": 0 },
                        "activeHours": { "start": "19", "end": "23" },
                        "traits": ["정리의 신","실행력 갓","내향인"],
                        "content": "Hi there, ~",
                        "portfolio": [
                          { "linkTitle": "깃헙 링크", "link": "https://github.com/xxx" },
                          { "linkTitle": "블로그", "link": "https://blog.example.com" }
                        ],
                        "location": { "latitude": 37.5665, "longitude": 126.9780, "address": "서울특별시 강남구 테헤란로…" }
                      }
                    }"""))),
            @ApiResponse(responseCode = "400", description = "유효성 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 400, "message": "포트폴리오 링크 제목은 1~15자여야 합니다.", "data": null }""")))
    })
    public ResponseEntity<ApiResponseDto<ProfileDto>> upsertPortfolio(
            @Valid @RequestBody PortfolioUpdateRequest req,
                                                                         @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.upsertPortfolio(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "포트폴리오 생성(수정)에 성공했습니다.", profile));
    }

    @PatchMapping
    @Operation(
            summary = "프로필 공개/비공개 토글",
            description = "현재 공개 상태를 반대로 변경합니다. (true → false, false → true)",
            security = @SecurityRequirement(name = "Authorization"),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "message": "프로필 공개 상태가 변경되었습니다.",
                          "data": {
                            "userId": 1,
                            "nickname": "홍길동",
                            "isPublic": false,
                            "mainPosition": "백엔드",
                            "traits": ["성실함", "소통"],
                            "techStacks": ["Spring Boot", "React"]
                          }
                        }
                        """))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 401, "message": "인증이 필요합니다.", "data": null }
                        """)))
    })
    public ResponseEntity<ApiResponseDto<ProfileDto>> toggleVisibility(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.toggleVisibility(me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "프로필 공개 여부 상태가 변경되었습니다.", profile));
    }

    @PatchMapping("/stacks")
    @Operation(
            summary = "기술 스택 수정",
            description = "보유 기술 스택을 전체 치환하고, 갱신된 전체 프로필을 반환합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "message": "요청이 성공적으로 처리되었습니다.",
                      "data": {
                        "userId": 1,
                        "nickname": "홍길동",
                        "isPublic": true,
                        "mainPosition": "백엔드",
                        "subPositions": ["프론트엔드"],
                        "traits": ["성실함", "소통"],
                        "techStacks": ["Java","Spring","JPA"],
                        "activeHours": { "start": 9, "end": 18 },
                        "location": { "latitude": 37.56, "longitude": 126.97, "address": "서울 ..." },
                        "profileImageUrl": "https://...",
                        "temperature": 36.5
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }
                    """)))
    })
    public ResponseEntity<ApiResponseDto<ProfileDto>> updateStacks(
            @Valid @RequestBody TechStacksUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updateTechStacks(req, me);  // ← 전체 프로필 반환으로 변경
        return ResponseEntity.ok(ApiResponseDto.of(200, "기술스택 수정에 성공했습니다.", profile));
    }
}