package goorm.ddok.reputation.controller;

import goorm.ddok.chat.service.ChatRoomService;
import goorm.ddok.chat.service.DmRequestCommandService;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.scheduler.ReputationRankingScheduler;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.reputation.dto.response.TemperatureMeResponse;
import goorm.ddok.reputation.dto.response.TemperatureRankResponse;
import goorm.ddok.reputation.dto.response.TemperatureRegionResponse;
import goorm.ddok.reputation.service.ReputationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/players/temperature/rank")
@Tag(name = "랭킹 API", description = "온도 랭킹 관련 API")
public class ReputationQueryController {

    private final ReputationQueryService reputationQueryService;
    private final ReputationRankingScheduler reputationRankingScheduler;
    private final ChatRoomService chatRoomService;
    private final DmRequestCommandService dmRequestService;

    @Operation(
            summary = "전체 온도 랭킹 TOP10 조회",
            description = "온도를 기준으로 상위 10명의 사용자 랭킹을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "요청이 성공적으로 처리되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "message": "요청이 성공적으로 처리되었습니다.",
                              "data": [
                                {
                                  "rank": 1,
                                  "userId": 1,
                                  "nickname": "캐구리",
                                  "temperature": 76.0,
                                  "mainPosition": "풀스택",
                                  "profileImageUrl": "https://cdn.example.com/images/players/1.jpg",
                                  "chatRoomId": null,
                                  "dmRequestPending": false,
                                  "isMine": false,
                                  "mainBadge": {
                                    "type": "login",
                                    "tier": "gold"
                                  },
                                  "abandonBadge": {
                                    "isGranted": false,
                                    "count": 0
                                  }
                                }
                              ]
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "랭킹 캐시 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 404, "message": "아직 랭킹 데이터가 준비되지 않았습니다.", "data": null }
                """))
            )
    })
    @GetMapping("/top10")
    public ApiResponseDto<List<TemperatureRankResponse>> getTop10TemperatureRank(
            @AuthenticationPrincipal CustomUserDetails currentUser
            ) {
        List<TemperatureRankResponse> cached = reputationRankingScheduler.getCachedTop10();

        Long meId = (currentUser != null) ? currentUser.getId() : null;

        List<TemperatureRankResponse> response = cached.stream()
                .map(r -> {
                    Long chatRoomId = null;
                    boolean dmPending = false;

                    if (meId != null && !meId.equals(r.getUserId())) {
                        chatRoomId = chatRoomService.findPrivateRoomId(meId, r.getUserId()).orElse(null);
                        dmPending = (chatRoomId != null)
                                || dmRequestService.isDmPendingOrAcceptedOrChatExists(meId, r.getUserId());
                    }

                    return r.toBuilder()
                            .IsMine(meId != null && r.getUserId().equals(meId))
                            .chatRoomId(chatRoomId)
                            .dmRequestPending(dmPending) // ✅ 실제 값
                            .build();
                })
                .toList();


        return ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", response);
    }

    @Operation(
            summary = "전체 온도 랭킹 TOP1 조회",
            description = "온도를 기준으로 1등 사용자 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "요청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "rank": 1,
                            "userId": 1,
                            "nickname": "캐구리",
                            "temperature": 76.0,
                            "mainPosition": "풀스택",
                            "profileImageUrl": "https://cdn.example.com/images/players/1.jpg",
                            "chatRoomId": null,
                            "dmRequestPending": false,
                            "isMine": false,
                            "mainBadge": { "type": "login", "tier": "gold" },
                            "abandonBadge": { "isGranted": false, "count": 0 },
                            "updatedAt": "2025-09-12T11:00:00+09:00"
                          }
                        }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 온도 정보 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "사용자 온도 정보를 찾을 수 없습니다.", "data": null }
                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "랭킹 캐시 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 404, "message": "아직 랭킹 데이터가 준비되지 않았습니다.", "data": null }
                """))
            )
    })
    @GetMapping("/top1")
    public ApiResponseDto<TemperatureRankResponse> getTop1TemperatureRank(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        TemperatureRankResponse cached = reputationRankingScheduler.peekCachedTop1();

        if (cached == null) {
            return ApiResponseDto.of(200, "조회 결과가 없습니다.", null);
        }

        Long meId = (currentUser != null) ? currentUser.getId() : null;

        Long chatRoomId = null;
        boolean dmPending = false;

        if (meId != null && !meId.equals(cached.getUserId())) {
            chatRoomId = chatRoomService.findPrivateRoomId(meId, cached.getUserId()).orElse(null);
            dmPending = (chatRoomId != null)
                    || dmRequestService.isDmPendingOrAcceptedOrChatExists(meId, cached.getUserId());
        }

        TemperatureRankResponse response = cached.toBuilder()
                .IsMine(meId != null && cached.getUserId().equals(meId))
                .chatRoomId(chatRoomId)
                .dmRequestPending(dmPending) // ✅ 실제 값
                .build();

        return ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", response);
    }

    @Operation(summary = "지역별 온도 랭킹 TOP1 조회", description = "서울, 경기, 강원, 충청, 경상, 전라, 제주 지역별 1위 사용자 조회")
    @GetMapping("/region")
    public ApiResponseDto<List<TemperatureRegionResponse>> getRegionTop1Rank(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        List<TemperatureRegionResponse> cached = reputationRankingScheduler.getCachedRegionTop1();

        Long meId = (currentUser != null) ? currentUser.getId() : null;

        List<TemperatureRegionResponse> response = cached.stream()
                .map(r -> {
                    Long chatRoomId = null;
                    boolean dmPending = false;

                    if (meId != null && !meId.equals(r.getUserId())) {
                        chatRoomId = chatRoomService.findPrivateRoomId(meId, r.getUserId()).orElse(null);
                        dmPending = (chatRoomId != null)
                                || dmRequestService.isDmPendingOrAcceptedOrChatExists(meId, r.getUserId());
                    }

                    return r.toBuilder()
                            .IsMine(meId != null && r.getUserId().equals(meId))
                            .chatRoomId(chatRoomId)
                            .dmRequestPending(dmPending) // ✅ 실제 값
                            .build();
                })
                .toList();

        return ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", response);
    }



    @Operation(
            summary = "내 온도 조회",
            description = "현재 로그인한 사용자의 온도를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "요청이 성공적으로 처리되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "message": "요청이 성공적으로 처리되었습니다.",
                              "data": {
                                "userId": 15,
                                "nickname": "똑똑한 똑똑이",
                                "temperature": 68.5,
                                "mainPosition": "백엔드",
                                "profileImageUrl": "https://cdn.example.com/images/players/15.jpg",
                                "mainBadge": {
                                  "type": "login",
                                  "tier": "gold"
                                },
                                "abandonBadge": {
                                  "isGranted": false,
                                  "count": 0
                                }
                              }
                            }
                            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 401, "message": "인증이 필요합니다.", "data": null }
                        """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "온도 정보 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 404, "message": "사용자 온도 정보를 찾을 수 없습니다.", "data": null }
                        """))
            )
    })
    @GetMapping("/me")
    public ApiResponseDto<TemperatureMeResponse> getMyTemperature(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ApiResponseDto.of(
                200,
                "요청이 성공적으로 처리되었습니다.",
                reputationQueryService.getMyTemperature(currentUser)
        );
    }
}
