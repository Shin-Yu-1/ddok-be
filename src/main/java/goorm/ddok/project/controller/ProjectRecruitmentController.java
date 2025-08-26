package goorm.ddok.project.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.dto.request.ProjectRecruitmentCreateRequest;
import goorm.ddok.project.dto.response.ProjectRecruitmentResponse;
import goorm.ddok.project.service.ProjectRecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            description = """
                새로운 프로젝트 모집글을 생성합니다. 요청은 Multipart/Form-Data 형식으로 전송해야 하며,
                request 필드에는 JSON 요청 본문을, bannerImage 필드에는 배너 이미지를 포함할 수 있습니다.
                배너 이미지를 null로 보낼 경우 기본 이미지가 자동 생성됩니다.
                
                - ONLINE 모드 -> 위치 정보 불필요
                - OFFLINE 모드 -> 위치 정보 필수
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로젝트 생성 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 201,
                  "message": "프로젝트 생성이 성공했습니다.",
                  "data": {
                    "projectId": 1,
                    "userId": 10,
                    "nickname": "고라니",
                    "leaderPosition": "백엔드",
                    "title": "구지라지",
                    "teamStatus": "RECRUITING",
                    "expectedStart": "2025-09-01",
                    "expectedMonth": 3,
                    "mode": "ONLINE",
                    "location": null,
                    "preferredAges": { "ageMin": 20, "ageMax": 30 },
                    "capacity": 5,
                    "bannerImageUrl": "https://cdn.example.com/images/project-banner.png",
                    "traits": ["실행력 갑", "성실함"],
                    "positions": ["백엔드", "프론트엔드", "디자이너"],
                    "detail": "저희 정말 멋진 웹을 만들거에요~ 하고 싶죠?"
                  }
                }
                """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (비로그인 사용자)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 401, "message": "인증이 필요합니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 시작일 과거",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 400, "message": "시작일은 오늘 이후여야 합니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 위치 정보 누락",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 400, "message": "위치 정보가 올바르지 않습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 연령대 범위 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 400, "message": "연령대 범위가 올바르지 않습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 리더 포지션 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 400, "message": "리더 포지션이 모집 포지션에 포함되어야 합니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 - 배너 이미지 업로드 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 500, "message": "배너 이미지 업로드에 실패했습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 - 프로젝트 저장 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 500, "message": "프로젝트 저장 중 오류가 발생했습니다.", "data": null }
                """)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ProjectRecruitmentResponse>> createProject(
            @RequestPart("request") @Valid ProjectRecruitmentCreateRequest request,
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ProjectRecruitmentResponse response = projectRecruitmentService.createProject(request, bannerImage, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.of(201, "프로젝트 생성이 성공했습니다.", response));
    }
}