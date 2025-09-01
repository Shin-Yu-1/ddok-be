package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.dto.ProfileDto;
import goorm.ddok.member.dto.request.*;
import goorm.ddok.member.service.PlayerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/players/profile")
@RequiredArgsConstructor
@Validated
public class PlayerProfileController {

    private final PlayerProfileService service;

    @PatchMapping("/positions")
    @Operation(summary = "포지션 수정")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updatePositions(
            @Valid @RequestBody PositionsUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updatePositions(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/traits")
    @Operation(summary = "나의 성향 수정")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateTraits(
            @Valid @RequestBody TraitsUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updateTraits(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/hours")
    @Operation(summary = "주 활동 시간 수정")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateActiveHours(
            @Valid @RequestBody ActiveHoursRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updateActiveHours(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/location")
    @Operation(summary = "주 활동 지역 수정")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateLocation(
            @Valid @RequestBody LocationUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updateLocation(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/content")
    @Operation(summary = "자기 소개 생성/수정")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> upsertContent(
            @Valid @RequestBody ContentUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.upsertContent(req, me); // 저장 엔티티 미정 → 응답만 포함(null)
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/portfolio")
    @Operation(summary = "포트폴리오 생성/수정")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> upsertPortfolio(
            @Valid @RequestBody PortfolioUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.upsertPortfolio(req, me); // 저장 엔티티 미정 → 응답만 포함(null)
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping
    @Operation(summary = "프로필 공개/비공개 설정")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateVisibility(
            @RequestParam(name = "isPublic") boolean isPublic,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        ProfileDto profile = service.updateVisibility(isPublic, me); // 저장 엔티티 미정 → 응답만 포함(null)
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("profile", profile)));
    }

    @PatchMapping("/stacks")
    @Operation(summary = "기술 스택 수정")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> updateStacks(
            @Valid @RequestBody TechStacksUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        service.updateTechStacks(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", Map.of("data", null)));
    }
}