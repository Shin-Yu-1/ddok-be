package goorm.ddok.player.controller;

import goorm.ddok.global.dto.PageResponse;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.player.dto.response.TechStackResponse;
import goorm.ddok.player.service.ProfileStackQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/players")
public class ProfileStackQueryController {
    private final ProfileStackQueryService profileStackQueryService;

    @Operation(
            summary = "프로필 기술 스택 조회",
            description = "특정 유저의 기술 스택 목록을 페이지네이션 형태로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 200,
                  "message": "요청이 성공적으로 처리되었습니다.",
                  "data": {
                    "pagination": {
                      "currentPage": 0,
                      "pageSize": 14,
                      "totalPages": 1,
                      "totalItems": 3
                    },
                    "items": [
                      { "techStack": "SpringBoot" },
                      { "techStack": "React" },
                      { "techStack": "MySQL" }
                    ]
                  }
                }
                """))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 404, "message": "사용자를 찾을 수 없습니다", "data": null }
                """)))
    })
    @GetMapping("/{userId}/profile/stacks")
    public ResponseEntity<ApiResponseDto<?>> getUserTechStacks(
            @PathVariable Long userId,
            @PageableDefault(size = 14) Pageable pageable
    ) {
        Page<TechStackResponse> stacks = profileStackQueryService.getUserTechStacks(userId, pageable);
        return ResponseEntity.ok(
                ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", goorm.ddok.global.dto.PageResponse.of(stacks))
        );
    }
}
