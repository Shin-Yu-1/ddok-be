package goorm.ddok.team.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.team.dto.request.WithdrawRequest;
import goorm.ddok.team.service.TeamCommandService;
import io.swagger.v3.oas.annotations.Operation;
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
public class TeamMemberCommandController {

    private final TeamCommandService teamMemberCommandService;

    @Operation(
            summary = "팀원 추방",
            description = """
                특정 팀에서 멤버를 추방합니다. (Soft Delete)
                - 팀 리더만 수행 가능
                - 추방 시 team_members.deletedAt, 관련 참가자 테이블(ProjectParticipant/StudyParticipant)도 동기화됩니다.
                """,
            security = @SecurityRequirement(name = "Authorization")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추방 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 200, "message": "팀원을 추방하였습니다.", "data": null }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (비로그인 사용자)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "message": "인증이 필요합니다.", "data": null }
                                    """))),
            @ApiResponse(responseCode = "403", description = "권한 부족 (리더만 가능 / 잘못된 추방 요청)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "리더 아님", value = """
                                            { "status": 403, "message": "해당 작업은 팀 리더만 가능합니다.", "data": null }
                                            """),
                                    @ExampleObject(name = "잘못된 요청", value = """
                                            { "status": 403, "message": "리더는 추방할 수 없습니다.", "data": null }
                                            """)
                            })),
            @ApiResponse(responseCode = "404", description = "리소스 없음 (팀 또는 팀원 없음)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "팀 없음", value = """
                                            { "status": 404, "message": "팀 정보를 찾을 수 없습니다.", "data": null }
                                            """),
                                    @ExampleObject(name = "팀원 없음", value = """
                                            { "status": 404, "message": "사용자를 찾을 수 없습니다.", "data": null }
                                            """)
                            })),
            @ApiResponse(responseCode = "409", description = "이미 처리된 요청 (이미 추방/탈퇴된 팀원)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 409, "message": "이미 추방된 팀원입니다.", "data": null }
                                    """)))
    })
    @PatchMapping("/{teamId}/members/{memberId}/expel")
    public ResponseEntity<ApiResponseDto<Void>> expelMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        teamMemberCommandService.expelMember(teamId, memberId, user);
        return ResponseEntity.ok(ApiResponseDto.of(200, "팀원을 추방하였습니다.", null));
    }

    @Operation(
            summary = "팀원 중도 하차",
            description = """
                특정 팀에서 본인이 직접 중도 하차합니다. (Soft Delete)
                - 본인만 수행 가능 (리더는 하차 불가)
                - 요청 시 확인 문구 "하차합니다." 필수
                - 하차 시 team_members.deletedAt, 관련 참가자 테이블(ProjectParticipant/StudyParticipant)도 동기화됩니다.
                """,
            security = @SecurityRequirement(name = "Authorization")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "하차 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 200, "message": "팀에서 중도 하차하였습니다.", "data": null }
                                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "잘못된 확인 문구", value = """
                                            { "status": 400, "message": "확인 문구가 올바르지 않습니다.", "data": null }
                                            """),
                                    @ExampleObject(name = "리더 하차 불가", value = """
                                            { "status": 400, "message": "리더는 하차할 수 없습니다.", "data": null }
                                            """)
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패 (비로그인 사용자)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 401, "message": "인증이 필요합니다.", "data": null }
                                    """))),
            @ApiResponse(responseCode = "403", description = "권한 부족 (본인만 하차 가능)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 403, "message": "접근 권한이 없습니다.", "data": null }
                                    """))),
            @ApiResponse(responseCode = "404", description = "리소스 없음 (팀 또는 팀원 없음)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "팀 없음", value = """
                                            { "status": 404, "message": "팀 정보를 찾을 수 없습니다.", "data": null }
                                            """),
                                    @ExampleObject(name = "팀원 없음", value = """
                                            { "status": 404, "message": "팀원을 찾을 수 없습니다.", "data": null }
                                            """)
                            })),
            @ApiResponse(responseCode = "409", description = "이미 처리된 요청 (이미 추방/탈퇴된 팀원)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 409, "message": "이미 추방되었거나 탈퇴한 팀원입니다.", "data": null }
                                    """)))
    })
    @PatchMapping("/{teamId}/members/{memberId}/withdraw")
    public ResponseEntity<ApiResponseDto<Void>> withdrawMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody WithdrawRequest request
    ) {
        teamMemberCommandService.withdrawMember(teamId, memberId, user, request.getConfirmText());
        return ResponseEntity.ok(ApiResponseDto.of(200, "팀에서 중도 하차하였습니다.", null));
    }

}
