package goorm.ddok.evaluation.controller;

import goorm.ddok.evaluation.dto.request.SaveScoresRequest;
import goorm.ddok.evaluation.dto.response.EvaluationModalResponse;
import goorm.ddok.evaluation.dto.response.SaveScoresResponse;
import goorm.ddok.evaluation.service.EvaluationCommandService;
import goorm.ddok.evaluation.service.EvaluationQueryService;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/{teamId}/evaluations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Team", description = "팀 관리 API")
public class EvaluationController {

    private final EvaluationQueryService queryService;
    private final EvaluationCommandService commandService;

    @GetMapping
    @Operation(summary = "동료 평가 모달 조회",
            security = @SecurityRequirement(name = "Authorization"))
    public ResponseEntity<ApiResponseDto<EvaluationModalResponse>> getModal(
            @PathVariable Long teamId,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        EvaluationModalResponse data = queryService.getModal(teamId, me.getId());
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", data));
    }

    @PostMapping("/{evaluationId}/scores")
    @Operation(summary = "동료 평가 저장",
            security = @SecurityRequirement(name = "Authorization"))
    public ResponseEntity<ApiResponseDto<SaveScoresResponse>> saveScores(
            @PathVariable Long teamId,
            @PathVariable Long evaluationId,
            @RequestBody @Validated SaveScoresRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        SaveScoresResponse data = commandService.saveScores(teamId, evaluationId, me.getId(), req);
        return ResponseEntity.ok(ApiResponseDto.of(200, "동료 평가가 저장되었습니다.", data));
    }
}