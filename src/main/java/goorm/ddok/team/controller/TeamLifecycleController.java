package goorm.ddok.team.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.team.dto.response.TeamCloseResponse;
import goorm.ddok.team.service.TeamLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team", description = "팀 관리 API")
public class TeamLifecycleController {

    private final TeamLifecycleService teamLifecycleService;

    @Operation(
            summary = "프로젝트/스터디 종료",
            description = "팀 타입을 판별하여 해당 모집(team_status)을 CLOSED로 전환하고, 동료 평가 라운드를 자동으로 오픈합니다.",
            security = @SecurityRequirement(name = "Authorization")
    )
    @PatchMapping("/{teamId}/close")
    public ResponseEntity<ApiResponseDto<TeamCloseResponse>> closeTeam(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        TeamCloseResponse data = teamLifecycleService.closeTeamAndOpenEvaluation(teamId, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "팀이 종료되었습니다.", data));
    }
}