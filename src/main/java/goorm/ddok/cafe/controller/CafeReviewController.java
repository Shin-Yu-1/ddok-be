package goorm.ddok.cafe.controller;

import goorm.ddok.cafe.dto.request.CafeReviewCreateRequest;
import goorm.ddok.cafe.dto.response.CafeReviewCreateResponse;
import goorm.ddok.cafe.service.CafeReviewService;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/map/cafes")
@RequiredArgsConstructor
@Tag(name = "Cafe Reviews", description = "카페 후기 API")
public class CafeReviewController {

    private final CafeReviewService cafeReviewService;

    @Operation(
            summary = "카페 후기 작성",
            security = @SecurityRequirement(name = "Authorization")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "검증 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "카페 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/{cafeId}/reviews")
    public ResponseEntity<ApiResponseDto<CafeReviewCreateResponse>> create(
            @PathVariable Long cafeId,
            @Validated @RequestBody CafeReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        if (me == null || me.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        CafeReviewCreateResponse data =
                cafeReviewService.createReview(cafeId, me.getUser().getId(), request);

        return ResponseEntity.ok(ApiResponseDto.of(200, "카페 후기 작성에 성공하였습니다.", data));
    }
}