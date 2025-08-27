package goorm.ddok.project.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.dto.request.ProjectDeleteRequest;
import goorm.ddok.project.service.ProjectRecruitmentDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "ProjectRecruitment-Delete", description = "프로젝트 삭제 API")
public class ProjectRecruitmentDeleteController {

    private final ProjectRecruitmentDeleteService service;

    @Operation(
            summary = "프로젝트 삭제",
            description = "리더 본인만 삭제 가능. confirmText가 '삭제합니다.' 와 정확히 일치해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 삭제가 성공했습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            { "status": 200, "message": "프로젝트 삭제가 성공했습니다.", "data": null }
                            """))),
            @ApiResponse(responseCode = "400", description = "확인 문구 불일치 또는 잘못된 요청",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            { "status": 400, "message": "확인 문구가 올바르지 않습니다.", "data": null }
                            """))),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "프로젝트 없음")
    })
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteProject(
            @PathVariable Long projectId,
            @RequestBody @Valid ProjectDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        service.deleteProject(projectId, request, user);
        return ResponseEntity.ok(ApiResponseDto.of(200, "프로젝트 삭제가 성공했습니다.", null));
    }
}