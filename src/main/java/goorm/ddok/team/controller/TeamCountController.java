package goorm.ddok.team.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.team.dto.response.TeamCountResponse;
import goorm.ddok.team.service.TeamCountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
@Tag(name = "Team", description = "팀 관리 API")
public class TeamCountController {

    private final TeamCountService teamCountService;


    @Operation(
            summary = "팀 카운트 조회",
            description = "모집 중인 프로젝트 팀, 모집 중인 스터디 팀, 진행 중인 전체 팀 개수를 조회합니다."
    )
    @GetMapping("/count")
    public ResponseEntity<ApiResponseDto<TeamCountResponse>> getTeamCount() {
        TeamCountResponse response = teamCountService.getTeamCountResponse();
        return ResponseEntity.ok(ApiResponseDto.of(200, "프로젝트/스터디/팀 카운트 조회 성공", response));
    }
}
