package goorm.ddok.project.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.dto.request.ProjectRecruitmentCreateRequest;
import goorm.ddok.project.dto.response.ProjectRecruitmentResponse;
import goorm.ddok.project.service.ProjectRecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "ProjectRectuitment", description = "프로젝트 모집 관련 API")
public class ProjectRecruitmentController {

    private final ProjectRecruitmentService projectRecruitmentService;

    @Operation(
            summary = "프로젝트 모집 글 생성",
            description = "프로젝트 모집 글과 배너 이미지를 업로드합니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ProjectRecruitmentResponse>> createProject(
            @RequestPart("request") @Valid ProjectRecruitmentCreateRequest request,
            @Parameter(
                    description = "배너 이미지 파일 (선택). 허용 확장자: jpeg, png, webp / 최대 5MB",
                    required = false
            )
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ProjectRecruitmentResponse response = projectRecruitmentService.createProject(request, bannerImage, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDto.of(201, "프로젝트 생성이 성공했습니다.", response));
    }
}