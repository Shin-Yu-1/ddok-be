package goorm.ddok.project.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.dto.request.ProjectRecruitmentUpdateRequest;
import goorm.ddok.project.dto.response.ProjectDetailResponse;
import goorm.ddok.project.dto.response.ProjectEditPageResponse;
import goorm.ddok.project.dto.response.ProjectUpdateResultResponse;
import goorm.ddok.project.service.ProjectRecruitmentEditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "프로젝트 API")
public class ProjectRecruitmentEditController {

    private final ProjectRecruitmentEditService service;

    @Operation(
            summary = "프로젝트 수정 페이지 조회",
            description = "수정 화면 진입 시 필요한 상세/통계를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정하기 페이지 조회가 성공했습니다.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "message": "수정하기 페이지 조회애 성공했습니다.",
                          "data": {
                            "projectId": 1,
                                                 "title": "구지라지",
                                                 "teamStatus": "RECRUITING",
                                                 "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0ZGREFCOSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7qtazsp4Drnbzsp4A8L3RleHQ+Cjwvc3ZnPgo=",
                                                 "traits": [
                                                     "정리의 신",
                                                     "실행력 갓",
                                                     "내향인"
                                                 ],
                                                 "capacity": 2,
                                                 "applicantCount": 0,
                                                 "mode": "offline",
                                                 "location": {
                                                     "address": null,
                                                     "region1depthName": null,
                                                     "region2depthName": null,
                                                     "region3depthName": null,
                                                     "roadName": null,
                                                     "mainBuildingNo": null,
                                                     "subBuildingNo": null,
                                                     "zoneNo": null,
                                                     "latitude": 37.566500,
                                                     "longitude": 126.978000
                                                 },
                                                 "preferredAges": {
                                                     "ageMin": 20,
                                                     "ageMax": 30
                                                 },
                                                 "expectedMonth": 3,
                                                 "startDate": "2025-09-30",
                                                 "detail": "저희 정말 멋진 웹을 만들거에요~ 하고 싶죠?",
                                                 "positions": [
                                                     {
                                                         "position": "백엔드",
                                                         "applied": 0,
                                                         "confirmed": 0,
                                                         "isApplied": false,
                                                         "isApproved": false,
                                                         "isAvailable": false
                                                     },
                                                     {
                                                         "position": "프론트엔드",
                                                         "applied": 0,
                                                         "confirmed": 0,
                                                         "isApplied": false,
                                                         "isApproved": false,
                                                         "isAvailable": false
                                                     },
                                                     {
                                                         "position": "디자이너",
                                                         "applied": 0,
                                                         "confirmed": 0,
                                                         "isApplied": false,
                                                         "isApproved": false,
                                                         "isAvailable": false
                                                     }
                                                 ],
                                                 "leader": {
                                                     "userId": 1,
                                                     "nickname": "멍한 백엔드",
                                                     "profileImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0OCIgaGVpZ2h0PSI0OCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0Y1Q0JBNyIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjE1IiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJtaWRkbGUiIGZvbnQtZmFtaWx5PSJJbnRlciI+66mN67CxPC90ZXh0Pgo8L3N2Zz4K",
                                                     "mainPosition": "백엔드",
                                                     "mainBadge": null,
                                                     "abandonBadge": null,
                                                     "temperature": null,
                                                     "decidedPosition": "백엔드",
                                                     "chatRoomId": null,
                                                     "dmRequestPending": false,
                                                     "isMine": true
                                                 },
                                                 "participants": [],
                                                 "isMine": true
                          }
                        }"""))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 401, "message": "인증이 필요합니다.", "data": null }"""))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 403, "message": "접근 권한이 없습니다.", "data": null }"""))),
            @ApiResponse(responseCode = "404", description = "프로젝트 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 404, "message": "프로젝트를 찾을 수 없습니다.", "data": null }""")))
    })
    @GetMapping("/{projectId}/edit")
    public ResponseEntity<ApiResponseDto<ProjectDetailResponse>> getEditPage(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ProjectDetailResponse data = service.getEditPage(projectId, user);
        return ResponseEntity.ok(ApiResponseDto.of(200, "수정하기 페이지 조회가 성공했습니다.", data));
    }


    @Operation(
            summary = "프로젝트 수정 저장",
            description = """
            multipart/form-data 요청. 
            - `request`: JSON (ProjectRecruitmentUpdateRequest)
            - `bannerImage`: 파일(선택, image/jpeg|png|webp, 최대 5MB)
            파일이 있으면 서버가 업로드 후 응답의 bannerImageUrl에 반영합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "message": "프로젝트 수정이 성공했습니다.",
                          "data": {
                            "projectId": 2,
                            "isMine": true,
                            "title": "구라라지 프로젝트",
                            "teamStatus": "RECRUITING",
                            "bannerImageUrl": "https://cdn.example.com/images/default.png",
                            "traits": ["정리의 신","실행력 갓","내향인"],
                            "capacity": 4,
                            "applicantCount": 6,
                            "mode": "online",
                            "location": null,
                            "preferredAges": { "ageMin": 20, "ageMax": 30 },
                            "expectedMonth": 3,
                            "startDate": "2025-09-10",
                            "detail": "저희 정말 멋진 웹을 만들거에요~ 하고 싶죠?",
                            "positions": [
                              { "position": "PM", "applied": 3, "confirmed": 2, "isApplied": false, "isApproved": false, "isAvailable": false },
                              { "position": "UI/UX", "applied": 3, "confirmed": 2, "isApplied": false, "isApproved": false, "isAvailable": false },
                              { "position": "백엔드", "applied": 3, "confirmed": 2, "isApplied": false, "isApproved": false, "isAvailable": false }
                            ],
                            "leader": {
                              "userId": 101,
                              "nickname": "개구라",
                              "profileImageUrl": "https://cdn.example.com/images/user101.png",
                              "mainPosition": "풀스택",
                              "temperature": 36.5,
                              "decidedPosition": "백엔드",
                              "isMine": true,
                              "chatRoomId": null,
                              "dmRequestPending": false
                            },
                            "participants": [
                              {
                                "userId": 201, "nickname": "개고루",
                                "profileImageUrl": "https://cdn.example.com/images/user201.png",
                                "mainPosition": "백엔드", "temperature": 36.5,
                                "decidedPosition": "백엔드", "isMine": false,
                                "chatRoomId": null, "dmRequestPending": true
                              }
                            ]
                          }
                        }"""))),
            @ApiResponse(responseCode = "400", description = "검증 실패/규칙 위반",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "과거 시작일", value = """
                                { "status": 400, "message": "시작일은 오늘 이후여야 합니다.", "data": null }"""),
                                    @ExampleObject(name = "위치 누락", value = """
                                { "status": 400, "message": "위치 정보가 올바르지 않습니다.", "data": null }"""),
                                    @ExampleObject(name = "연령 범위", value = """
                                { "status": 400, "message": "연령대 범위가 올바르지 않습니다.", "data": null }"""),
                                    @ExampleObject(name = "포지션 오류", value = """
                                { "status": 400, "message": "모집 포지션이 올바르지 않습니다.", "data": null }""")
                            })),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 401, "message": "인증이 필요합니다.", "data": null }"""))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 403, "message": "접근 권한이 없습니다.", "data": null }"""))),
            @ApiResponse(responseCode = "404", description = "프로젝트 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 404, "message": "프로젝트를 찾을 수 없습니다.", "data": null }"""))),
            @ApiResponse(responseCode = "500", description = "배너 업로드 실패 등 서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        { "status": 500, "message": "배너 이미지 업로드에 실패했습니다.", "data": null }""")))
    })
    @PatchMapping(value = "/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ProjectUpdateResultResponse>> update(
            @PathVariable Long projectId,
            @RequestPart("request") @Valid ProjectRecruitmentUpdateRequest request,
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ProjectUpdateResultResponse data = service.updateProject(projectId, request, bannerImage, user);
        return ResponseEntity.ok(ApiResponseDto.of(200, "프로젝트 수정이 성공했습니다.", data));
    }
}