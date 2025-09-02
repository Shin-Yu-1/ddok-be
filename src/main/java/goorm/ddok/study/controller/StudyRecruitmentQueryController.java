package goorm.ddok.study.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.study.dto.response.StudyRecruitmentDetailResponse;
import goorm.ddok.study.service.StudyRecruitmentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyRecruitmentQueryController {

    private final StudyRecruitmentQueryService studyRecruitmentQueryService;

    @Operation(
            summary = "스터디 모집글 상세 조회",
            description = """
        특정 스터디 모집글의 상세 정보를 조회합니다.

        - 로그인 사용자일 경우 isMine, isApplied, isApproved 값이 반영됩니다.
        - online 모드일 경우 address = null
        - offline 모드일 경우 address = "광역시/도 시/군/구 동/읍/면 도로명 본번-부번"
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
            {
              "status": 200,
              "message": "스터디 모집글 상세 조회가 성공했습니다.",
              "data": {
                "studyId": 2,
                "title": "프리토킹 스터디",
                "studyType": "취업/면접",
                "isMine": true,
                "isApplied": false,
                "isApproved": false,
                "teamStatus": "RECRUITING",
                "bannerImageUrl": "https://cdn.example.com/images/default.png",
                "traits": ["정리의 신", "실행력 갓", "내향인"],
                "capacity": 4,
                "applicantCount": 6,
                "mode": "online",
                "address": null,
                "preferredAges": { "ageMin": 20, "ageMax": 30 },
                "expectedMonth": 3,
                "startDate": "2025-09-10",
                "detail": "저희 정말 멋진 영어공부를 할거예요~ 하고 싶죠?",
                "leader": { },
                "participants": [ ],
                "participantsCount": 3
              }
            }
            """))),
            @ApiResponse(responseCode = "404", description = "스터디 없음/삭제됨",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
            { "status": 404, "message": "존재하지 않는 스터디입니다.", "data": null }
            """))),
            @ApiResponse(responseCode = "404", description = "리더 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
            { "status": 404, "message": "리더 정보를 찾을 수 없습니다.", "data": null }
            """)))
    })
    @GetMapping("/{studyId}")
    public ResponseEntity<ApiResponseDto<StudyRecruitmentDetailResponse>> getStudyDetail(
            @PathVariable @NotNull Long studyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StudyRecruitmentDetailResponse response =
                studyRecruitmentQueryService.getStudyDetail(studyId, userDetails);

        return ResponseEntity.ok(ApiResponseDto.of(200, "스터디 모집글 상세 조회가 성공했습니다.", response));
    }
}