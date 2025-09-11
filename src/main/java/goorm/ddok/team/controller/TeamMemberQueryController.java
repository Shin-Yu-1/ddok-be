package goorm.ddok.team.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.team.dto.response.TeamMembersResponse;
import goorm.ddok.team.service.TeamMemberQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
@Tag(name = "Team", description = "팀 관리 API")
public class TeamMemberQueryController {

    private final TeamMemberQueryService teamMemberQueryService;

    @Operation(
            summary = "참여 확정자 조회",
            description = """
                특정 팀(teamId)의 확정된 팀원 목록을 조회합니다.
                리더 및 승인된 팀원만 접근할 수 있으며, 승인된 멤버(LEADER/MEMBER)만 조회됩니다.
                """,
            security = @SecurityRequirement(name = "Authorization"),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi...")),
                    @Parameter(name = "teamId", in = ParameterIn.PATH, required = true,
                            description = "팀 ID"),
                    @Parameter(name = "page", in = ParameterIn.QUERY, required = false,
                            description = "페이지 번호 (기본값: 0)", example = "0"),
                    @Parameter(name = "size", in = ParameterIn.QUERY, required = false,
                            description = "페이지 크기 (기본값: 10)", example = "10")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "message": "요청이 성공적으로 처리되었습니다.",
                                      "data": {
                                        "pagination": {
                                          "currentPage": 0,
                                          "pageSize": 4,
                                          "totalPages": 1,
                                          "totalItems": 2
                                        },
                                        "teamId": 2,
                                        "teamType": "PROJECT",
                                        "teamTitle": "똑DDOK!",
                                        "teamStatus": "RECRUITING"
                                        "recruitmentId": 33,
                                        "isLeader": true,
                                        "items": [
                                          {
                                            "memberId": 201,
                                            "decidedPosition": "풀스택",
                                            "role": "LEADER",
                                            "joinedAt": "2025-09-01T12:30:00",
                                            "isMine": false,
                                            "user": {
                                              "userId": 11,
                                              "nickname": "용",
                                              "profileImageUrl": "https://~",
                                              "temperature": 36.5,
                                              "mainPosition": "풀스택",
                                              "chatRoomId": null,
                                              "dmRequestPending": false,
                                              "mainBadge": {
                                                "type": "login",
                                                "tier": "gold"
                                              },
                                              "abandonBadge": {
                                                "isGranted": false,
                                                "count": 0
                                              }
                                            }
                                          },
                                          {
                                            "memberId": 202,
                                            "decidedPosition": "디자이너",
                                            "role": "MEMBER",
                                            "joinedAt": "2025-09-01T13:10:00",
                                            "isMine": true,
                                            "user": {
                                              "userId": 12,
                                              "nickname": "은",
                                              "profileImageUrl": "https://~",
                                              "temperature": 36.7,
                                              "mainPosition": "프론트엔드",
                                              "chatRoomId": null,
                                              "dmRequestPending": false,
                                              "mainBadge": {
                                                "type": "login",
                                                "tier": "silver"
                                              },
                                              "abandonBadge": {
                                                "isGranted": true,
                                                "count": 2
                                              }
                                            }
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (비로그인 사용자)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "message": "인증이 필요합니다.", "data": null }
                                    """))),
            @ApiResponse(responseCode = "403", description = "팀원이 아님",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 403, "message": "팀에 소속되지 않은 사용자는 접근할 수 없습니다.", "data": null }
                                    """))),
            @ApiResponse(responseCode = "404", description = "팀 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "message": "팀 정보를 찾을 수 없습니다.", "data": null }
                                    """))),
            @ApiResponse(responseCode = "404", description = "포지션 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 404, "message": "해당 포지션을 찾을 수 없습니다.", "data": null }
                        """)))
    })
    @GetMapping("/{teamId}/members")
    public ResponseEntity<ApiResponseDto<TeamMembersResponse>> getMembers(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        TeamMembersResponse response = teamMemberQueryService.getMembers(teamId, user, page, size);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", response));
    }
}