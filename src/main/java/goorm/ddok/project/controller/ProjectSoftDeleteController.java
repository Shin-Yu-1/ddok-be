package goorm.ddok.project.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.dto.request.ProjectDeleteRequest;
import goorm.ddok.project.service.ProjectRecruitmentSoftDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "프로젝트 API")
public class ProjectSoftDeleteController {

    private final ProjectRecruitmentSoftDeleteService service;

    @Operation(summary = "프로젝트 삭제(Soft Delete)",
            description = """
                프로젝트를 실제로 삭제하지 않고 deletedAt을 채워 비활성화합니다.
                리더만 수행 가능하며, 요청 본문의 confirmText는 정확히 '삭제합니다.' 이어야 합니다.
                """,
    security = @SecurityRequirement(name = "Authorization"),
    parameters = {
        @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                description = "Bearer {accessToken}",
                examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로젝트 삭제가 성공했습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            { "status": 200, "message": "프로젝트 삭제가 성공했습니다.", "data": null }
                            """))),
            @ApiResponse(responseCode = "400", description = "잘못된 확인 문구",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            { "status": 400, "message": "확인 문구가 올바르지 않습니다.", "data": null }
                            """))),
            @ApiResponse(responseCode = "401", description = "미인증 사용자",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음(리더 아님)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "프로젝트 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteProject(
            @PathVariable Long projectId,
            @RequestBody @Valid ProjectDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        service.softDelete(projectId, request, user);
        return ResponseEntity.ok(ApiResponseDto.of(200, "프로젝트 삭제가 성공했습니다.", null));
    }
}