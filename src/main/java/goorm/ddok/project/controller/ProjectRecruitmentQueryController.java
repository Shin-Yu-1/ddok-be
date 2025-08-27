package goorm.ddok.project.controller;


import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.dto.response.ProjectDetailResponse;
import goorm.ddok.project.service.ProjectRecruitmentReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectRecruitmentReadController {

    private final ProjectRecruitmentReadService projectRecruitmentReadService;

    /**
     * 프로젝트 모집글 상세 조회
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponseDto<ProjectDetailResponse>> getProjectDetail(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ProjectDetailResponse response = projectRecruitmentReadService.getProjectDetail(projectId, user);
        return ResponseEntity.ok(
                ApiResponseDto.of(200, "프로젝트 모집글 상세 조회가 성공했습니다.", response)
        );
    }
}