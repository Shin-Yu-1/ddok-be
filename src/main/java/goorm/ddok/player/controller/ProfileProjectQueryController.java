package goorm.ddok.player.controller;

import goorm.ddok.global.dto.PageResponse;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.player.dto.response.ProjectParticipationResponse;
import goorm.ddok.player.service.ProfileProjectQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 API")
public class ProfileProjectQueryController {

    private final ProfileProjectQueryService profileProjectQueryService;

    @Operation(
            summary = "참여 프로젝트 목록 조회",
            description = """
                특정 사용자가 참여 중이거나 참여했던 프로젝트 목록을 조회합니다.
                결과는 페이지네이션(pagination) 구조로 제공됩니다.
                
                - 참여 이력이 없으면 `items`는 빈 배열로 내려갑니다.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참여 프로젝트 조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "message": "요청이 성공적으로 처리되었습니다.",
                                      "data": {
                                        "pagination": {
                                          "currentPage": 0,
                                          "pageSize": 4,
                                          "totalPages": 1,
                                          "totalItems": 2
                                        },
                                        "items": [
                                          {
                                            "projectId": 1,
                                            "teamId": 2,
                                            "title": "구지라지 프로젝트",
                                            "teamStatus": "CLOSED",
                                            "location": {
                                              "latitude": 37.5665,
                                              "longitude": 126.9780,
                                              "address": "서울특별시 강남구 테헤란로…"
                                            },
                                            "period": {
                                              "start": "2025-08-01",
                                              "end": "2025-09-15"
                                            }
                                          }
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                    { "status": 404, "message": "존재하지 않는 사용자입니다.", "data": null }
                                    """))),
            @ApiResponse(responseCode = "404", description = "팀 정보 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                                { "status": 404, "message": "팀 정보를 찾을 수 없습니다.", "data": null }
                                """)))
    })
    @GetMapping("/{userId}/profile/projects")
    public ResponseEntity<ApiResponseDto<?>> getUserProjects(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        Page<ProjectParticipationResponse> projects = profileProjectQueryService.getUserProjects(userId, page, size);
        return ResponseEntity.ok(
                ApiResponseDto.of(200, "요청이 성공적으로 처리되었습니다.", PageResponse.of(projects))
        );
    }
}
