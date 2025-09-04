package goorm.ddok.study.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.study.dto.request.StudyDeleteRequest;
import goorm.ddok.study.service.StudyDeleteService;
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
@RequestMapping("/api/studies")
@RequiredArgsConstructor
@Tag(name = "Study", description = "스터디 API")
public class StudyDeleteController {

    private final StudyDeleteService studyDeleteService;

    @Operation(
            summary = "스터디 삭제",
            description = """
                스터디 리더만 삭제할 수 있습니다. (Soft Delete: deletedAt만 세팅)
                요청 본문의 확인 문구는 **정확히 "삭제합니다."** 여야 합니다.
                """,
    security = @SecurityRequirement(name = "Authorization"),
    parameters = {
        @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                description = "Bearer {accessToken}",
                examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
    }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스터디 삭제 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "message": "스터디 삭제가 성공했습니다.",
                      "data": null
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(확인 문구 불일치 등)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 400, "message": "요청이 올바르지 않습니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "403", description = "권한 없음(리더 아님)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 403, "message": "접근 권한이 없습니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "404", description = "스터디 없음/이미 삭제됨",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "존재하지 않는 스터디입니다.", "data": null }
                    """))),
    })
    @DeleteMapping("/{studyId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteStudy(
            @PathVariable Long studyId,
            @RequestBody @Valid StudyDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        studyDeleteService.delete(studyId, request, user);
        return ResponseEntity.ok(ApiResponseDto.of(200, "스터디 삭제가 성공했습니다.", null));
    }
}