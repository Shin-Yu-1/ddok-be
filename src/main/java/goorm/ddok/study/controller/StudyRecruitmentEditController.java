package goorm.ddok.study.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.study.dto.request.StudyRecruitmentUpdateRequest;
import goorm.ddok.study.dto.response.StudyRecruitmentDetailResponse;
import goorm.ddok.study.service.StudyRecruitmentEditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
@Tag(name = "StudyRecruitmentEdit", description = "스터디 모집글 수정/수정페이지 조회 API")
public class StudyRecruitmentEditController {

    private final StudyRecruitmentEditService Service;

    @Operation(
            summary = "스터디 수정페이지 조회",
            description = """
                스터디 수정페이지 데이터를 조회합니다. (리더만 접근 가능)
                
                - 리더가 아닌 사용자가 접근 시 403
                - 삭제된 스터디거나 존재하지 않으면 404
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "status": 200,
                      "message": "스터디 수정페이지 조회가 성공했습니다.",
                      "data": {
                        "studyId": 1,
                        "title": "프리토킹(오프라인 전환)",
                        "studyType": "취업/면접",
                        "isMine": true,
                        "teamStatus": "RECRUITING",
                        "bannerImageUrl": "https://cdn.example.com/images/default.png",
                        "traits": ["회화중심","오프라인네트워킹"],
                        "capacity": 6,
                        "applicantCount": 3,
                        "mode": "online",
                        "address": null,
                        "preferredAges": null,
                        "expectedMonth": 2,
                        "startDate": "2025-10-20",
                        "detail": "이제 매주 토요일 오프라인 진행합니다.",
                        "leader": {
                          "userId": 1,
                          "nickname": "hongseonyeon",
                          "profileImageUrl": "https://picsum.photos/seed/1/200/200",
                          "mainPosition": "프론트엔드",
                          "mainBadge": null,
                          "abandonBadge": null,
                          "temperature": 36.5,
                          "isMine": true,
                          "chatRoomId": null,
                          "dmRequestPending": false
                        },
                        "participants": [
                          {
                            "userId": 3,
                            "nickname": "hayun77",
                            "profileImageUrl": "https://picsum.photos/seed/2/200/200",
                            "mainPosition": "백엔드",
                            "mainBadge": null,
                            "abandonBadge": null,
                            "temperature": null,
                            "isMine": false,
                            "chatRoomId": null,
                            "dmRequestPending": false
                          }
                        ],
                        "participantsCount": 1
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (비로그인 사용자)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "403", description = "리더가 아님",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 403, "message": "접근 권한이 없습니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "404", description = "스터디 없음/삭제됨",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "존재하지 않는 스터디입니다.", "data": null }
                    """)))
    })
    @GetMapping("/{studyId}/edit")
    public ResponseEntity<ApiResponseDto<StudyRecruitmentDetailResponse>> getEditPage(
            @PathVariable Long studyId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        StudyRecruitmentDetailResponse data = Service.getEditPage(studyId, user);
        return ResponseEntity.ok(ApiResponseDto.of(200, "스터디 수정페이지 조회가 성공했습니다.", data));
    }

    @Operation(
            summary = "스터디 수정 저장 (multipart/form-data)",
            description = """
                스터디 수정 내용을 저장합니다. (리더만 가능)
                
                - 파일과 JSON을 함께 전송할 때 사용하세요.
                - 파일 없이 JSON만 전송하려면 아래 `application/json` 엔드포인트를 사용하세요.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "status": 200,
                      "message": "스터디 수정이 성공했습니다.",
                      "data": {
                        "studyId": 1,
                        "title": "온라인 전환",
                        "studyType": "자격증 취득",
                        "isMine": true,
                        "teamStatus": "RECRUITING",
                        "bannerImageUrl": "https://cdn.example.com/images/default.png",
                        "traits": ["정리의 신","성실함"],
                        "capacity": 5,
                        "applicantCount": 2,
                        "mode": "online",
                        "address": null,
                        "preferredAges": { "ageMin": 20, "ageMax": 30 },
                        "expectedMonth": 3,
                        "startDate": "2025-11-10",
                        "detail": "온라인으로 전환합니다.",
                        "leader": {
                          "userId": 1,
                          "nickname": "hongseonyeon",
                          "profileImageUrl": "https://picsum.photos/seed/1/200/200",
                          "mainPosition": "프론트엔드",
                          "mainBadge": null,
                          "abandonBadge": null,
                          "temperature": 36.5,
                          "isMine": true,
                          "chatRoomId": null,
                          "dmRequestPending": false
                        },
                        "participants": [],
                        "participantsCount": 0
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(날짜/위치/연령대 등)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "과거 시작일", value = """
                                    { "status": 400, "message": "시작일은 오늘 이후여야 합니다.", "data": null }
                                    """),
                                    @ExampleObject(name = "위치 누락", value = """
                                    { "status": 400, "message": "위치 정보가 올바르지 않습니다.", "data": null }
                                    """),
                                    @ExampleObject(name = "연령대 범위 오류", value = """
                                    { "status": 400, "message": "연령대 범위가 올바르지 않습니다.", "data": null }
                                    """),
                                    @ExampleObject(name = "연령 10단위 오류", value = """
                                    { "status": 400, "message": "연령은 10단위(예: 20, 30, 40)만 허용합니다.", "data": null }
                                    """)
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "403", description = "리더가 아님",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 403, "message": "접근 권한이 없습니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "404", description = "스터디 없음/삭제됨",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "존재하지 않는 스터디입니다.", "data": null }
                    """)))
    })
    @PatchMapping(
            value = "/{studyId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseDto<StudyRecruitmentDetailResponse>> updateStudyWithFile(
            @PathVariable Long studyId,
            @RequestPart("request") StudyRecruitmentUpdateRequest request,
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StudyRecruitmentDetailResponse data =
                Service.updateStudy(studyId, request, bannerImage, userDetails);
        return ResponseEntity.ok(ApiResponseDto.of(200, "스터디 수정이 성공했습니다.", data));
    }

    @Operation(
            summary = "스터디 수정 저장 (application/json)",
            description = """
                스터디 수정 내용을 저장합니다. (리더만 가능)
                
                - JSON 바디만 전송할 때 사용하세요.
                - 배너 이미지를 변경하려면 multipart/form-data 엔드포인트를 사용하세요.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                    {
                      "status": 200,
                      "message": "스터디 수정을 성공했습니다.",
                      "data": {
                        "studyId": 1,
                        "title": "프리토킹(온라인)",
                        "studyType": "취업/면접",
                        "isMine": true,
                        "teamStatus": "ONGOING",
                        "bannerImageUrl": "https://cdn.example.com/images/default.png",
                        "traits": ["집중","꾸준함"],
                        "capacity": 4,
                        "applicantCount": 1,
                        "mode": "online",
                        "location": null,
                        "preferredAges": null,
                        "expectedMonth": 2,
                        "startDate": "2025-10-20",
                        "detail": "온라인으로 전환합니다.",
                        "leader": {
                          "userId": 1,
                          "nickname": "hongseonyeon",
                          "profileImageUrl": "https://picsum.photos/seed/1/200/200",
                          "mainPosition": "프론트엔드",
                          "mainBadge": null,
                          "abandonBadge": null,
                          "temperature": 36.5,
                          "isMine": true,
                          "chatRoomId": null,
                          "dmRequestPending": false
                        },
                        "participants": [],
                        "participantsCount": 0
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(날짜/위치/연령대 등)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "과거 시작일", value = """
                                    { "status": 400, "message": "시작일은 오늘 이후여야 합니다.", "data": null }
                                    """),
                                    @ExampleObject(name = "위치 누락", value = """
                                    { "status": 400, "message": "위치 정보가 올바르지 않습니다.", "data": null }
                                    """),
                                    @ExampleObject(name = "연령대 범위 오류", value = """
                                    { "status": 400, "message": "연령대 범위가 올바르지 않습니다.", "data": null }
                                    """)
                            })),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 401, "message": "인증이 필요합니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "403", description = "리더가 아님",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 403, "message": "접근 권한이 없습니다.", "data": null }
                    """))),
            @ApiResponse(responseCode = "404", description = "스터디 없음/삭제됨",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                    { "status": 404, "message": "존재하지 않는 스터디입니다.", "data": null }
                    """)))
    })
    @PatchMapping(
            value = "/{studyId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseDto<StudyRecruitmentDetailResponse>> updateStudyJsonOnly(
            @PathVariable Long studyId,
            @RequestBody StudyRecruitmentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StudyRecruitmentDetailResponse data =
                Service.updateStudy(studyId, request, null, userDetails);
        return ResponseEntity.ok(ApiResponseDto.of(200, "스터디 수정을 성공했습니다.", data));
    }
}