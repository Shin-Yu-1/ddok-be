package goorm.ddok.study.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.study.dto.request.StudyRecruitmentCreateRequest;
import goorm.ddok.study.dto.response.StudyRecruitmentCreateResponse;
import goorm.ddok.study.service.StudyRecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyRecruitmentController {

    private final StudyRecruitmentService studyRecruitmentService;

    @Operation(
            summary = "스터디 모집글 생성",
            description = """
                새로운 스터디 모집글을 생성합니다. 요청은 Multipart/Form-Data 형식으로 전송해야 하며, 
                request 필드에는 JSON 요청 본문을, bannerImage 필드에는 배너 이미지를 포함할 수 있습니다. 
                배너 이미지를 null로 보낼경우 기본 이미지가 자동 생성 됩니다.
                
                - ONLINE 모드 -> 위치 정보 불필요
                - OFFLINE 모드 -> 위치 정보 필수
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스터디 생성 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 201,
                  "message": "스터디 생성이 성공했습니다.",
                  "data": {
                    "studyId": 1,
                    "userId": 10,
                    "nickname": "고라니",
                    "teamStatus": "RECRUITING",
                    "title": "백엔드 스터디",
                    "expectedStart": "2025-09-01",
                    "expectedMonth": 3,
                    "mode": "ONLINE",
                    "location": null,
                    "preferredAges": { "ageMin": 20, "ageMax": 30 },
                    "capacity": 5,
                    "bannerImageUrl": "https://cdn.example.com/images/study-banner.png",
                    "traits": ["실행력 갑", "성실함"],
                    "studyType": "자기 개발",
                    "detail": "알고리즘 문제 풀이 및 코드 리뷰 중심 스터디"
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
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 스터디 유형 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
            { "status": 400, "message": "스터디 유형은 필수이며 올바른 값이어야 합니다.", "data": null }
            """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 - 배너 이미지 업로드 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
            { "status": 500, "message": "배너 이미지 업로드에 실패했습니다.", "data": null }
            """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 - 스터디 저장 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
            { "status": 500, "message": "스터디 저장 중 오류가 발생했습니다.", "data": null }
            """)))

    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<StudyRecruitmentCreateResponse>> createStudy(
            @RequestPart("request") @Valid StudyRecruitmentCreateRequest request,
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        StudyRecruitmentCreateResponse response = studyRecruitmentService.createStudy(request, bannerImage, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.of(201, "스터디 생성이 성공했습니다.", response));
    }

    @Operation(
            summary = "스터디 참여 신청/취소",
            description = "스터디 참여 희망 의사를 신청하거나 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신청/취소 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "신청 성공", value = """
                                    {
                                      "status": 200,
                                      "message": "스터디 참여 희망 의사가 신청되었습니다.",
                                      "data": { "isApplied": true }
                                    }
                                    """),
                                    @ExampleObject(name = "취소 성공", value = """
                                    {
                                      "status": 200,
                                      "message": "스터디 참여 희망 의사가 취소되었습니다.",
                                      "data": { "isApplied": false }
                                    }
                                    """)
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            { "status": 401, "message": "인증이 필요합니다.", "data": null }
                            """))),
            @ApiResponse(responseCode = "403", description = "리더는 참여 불가",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            { "status": 403, "message": "리더는 참여 신청을 할 수 없습니다.", "data": null }
                            """))),
            @ApiResponse(responseCode = "404", description = "스터디 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            { "status": 404, "message": "존재하지 않는 스터디입니다.", "data": null }
                            """)))
    })
    @PostMapping("/{studyId}/join")
    public ResponseEntity<ApiResponseDto<Map<String, Boolean>>> toggleJoin(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId
    ) {
        boolean isApplied = studyRecruitmentService.toggleJoin(userDetails, studyId);
        String message = isApplied ?
                "스터디 참여 희망 의사가 신청되었습니다." :
                "스터디 참여 희망 의사가 취소되었습니다.";

        return ResponseEntity.ok(ApiResponseDto.of(200, message, Map.of("isApplied", isApplied)));
    }
}
