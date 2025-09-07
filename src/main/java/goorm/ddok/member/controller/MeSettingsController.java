package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.global.security.token.ReauthRequired;
import goorm.ddok.member.dto.request.*;
import goorm.ddok.member.dto.response.PasswordVerifyResponse;
import goorm.ddok.member.dto.response.SettingsPageResponse;
import goorm.ddok.member.service.MeSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static goorm.ddok.global.security.token.ReauthRequiredInterceptor.HEADER;

@RestController
@RequestMapping("/api/me/settings")
@RequiredArgsConstructor
@Tag(name = "Me Settings", description = "내 정보 - 개인정보변경 API")
public class MeSettingsController {

    private final MeSettingsService service;

    @Operation(
            summary = "비밀번호 검증 및 reauthToken 발급",
            description = "민감 작업 전에 본인 인증을 수행하고, 단기 reauthToken을 발급합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = PasswordVerifyRequest.class),
                            examples = @ExampleObject(value = """
                            {
                              "password": "Test1324!"
                            }"""))
            ),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "본인 인증 성공 및 reauthToken 발급",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "message": "본인 인증에 성공했습니다.",
                              "data": { "reauthToken": "REAUTHTOKEN_VALUE" }
                            }"""))),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치 / 인증 실패")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<PasswordVerifyResponse>> verifyPassword(
            @Valid @RequestBody PasswordVerifyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String token = service.verifyPasswordAndIssueReauthToken(request, userDetails);
        return ResponseEntity.ok(ApiResponseDto.of(200, "본인 인증에 성공했습니다.", new PasswordVerifyResponse(token)));
    }

    /* =========================
     *  개인정보변경 페이지 조회
     * ========================= */
    @GetMapping
    @Operation(summary = "개인정보변경 페이지 조회",
            security = { @SecurityRequirement(name = "Authorization"), @SecurityRequirement(name = "Reauth") },
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi...")),
                    @Parameter(name = HEADER, in = ParameterIn.HEADER, required = true,
                            description = "reauthToken (비밀번호 검증으로 발급)",
                            examples = @ExampleObject(value = "reauth_0d2d5f..."))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "message": "개인정보변경 페이지 조회에 성공했습니다.",
                              "data": {
                                "userId": 1,
                                "profileImageUrl": "",
                                "username": "곽두철",
                                "nickname": "똑똑한 똑똑이",
                                "birthDate": "1997-10-10",
                                "email": "User@email.com",
                                "phoneNumber": "01012345678",
                                "password": "********"
                              }
                            }""")))
    })
    public ResponseEntity<ApiResponseDto<SettingsPageResponse>> getSettings(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        SettingsPageResponse data = service.getSettings(me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "개인정보변경 페이지 조회에 성공했습니다.", data));
    }

    /* =========================
     *  프로필 이미지 수정 (JSON: URL)
     * ========================= */
    @PatchMapping(value = "/image", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "프로필 이미지 수정 (URL)",
            security = { @SecurityRequirement(name = "Authorization"), @SecurityRequirement(name = "Reauth") },
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi...")),
                    @Parameter(name = HEADER, in = ParameterIn.HEADER, required = true,
                            description = "reauthToken (비밀번호 검증으로 발급)",
                            examples = @ExampleObject(value = "reauth_0d2d5f..."))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "message": "프로필 이미지 수정에 성공했습니다.",
                              "data": {
                                "userId": 1,
                                "profileImageUrl": "https://cdn.../me.png",
                                "username": "곽두철",
                                "nickname": "똑똑한 똑똑이",
                                "birthDate": "1997-10-10",
                                "email": "User@email.com",
                                "phoneNumber": "01012345678",
                                "password": "********"
                              }
                            }""")))
    })
    public ResponseEntity<ApiResponseDto<SettingsPageResponse>> updateImageByUrl(
            @Valid @RequestBody ProfileImageUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        SettingsPageResponse updated = service.updateProfileImageByUrl(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "프로필 이미지 수정에 성공했습니다.", updated));
    }

    /* =========================
     *  프로필 이미지 수정 (파일 업로드)
     * ========================= */
    @PatchMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "프로필 이미지 수정 (업로드)",
            description = "multipart/form-data 로 이미지를 업로드해서 프로필 이미지를 교체합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = false,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ProfileImageUploadForm.class)
                    )
            )
    )
    public ResponseEntity<ApiResponseDto<SettingsPageResponse>> updateImageByUpload(
            @ModelAttribute ProfileImageUploadForm form,                 // ← 폼 객체로 받기
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        SettingsPageResponse updated = service.updateProfileImageByUpload(
                form.getFile(),
                Boolean.TRUE.equals(form.getForcePlaceholder()),
                me
        );
        return ResponseEntity.ok(ApiResponseDto.of(200, "프로필 이미지 수정에 성공했습니다.", updated));
    }

//    public ResponseEntity<ApiResponseDto<SettingsPageResponse>> updateImageByUpload(
//            @RequestPart(value = "file", required = false) MultipartFile file,
//            @RequestPart(value = "forcePlaceholder", required = false) Boolean forcePlaceholder,
//            @AuthenticationPrincipal CustomUserDetails me
//    ) {
//        SettingsPageResponse updated = service.updateProfileImageByUpload(file, Boolean.TRUE.equals(forcePlaceholder), me);
//        return ResponseEntity.ok(ApiResponseDto.of(200, "프로필 이미지 수정에 성공했습니다.", updated));
//    }

    /* =========================
     *  닉네임 수정 (수정 후 개인정보 블록 반환)
     * ========================= */
    @PatchMapping("/nickname")
    @Operation(summary = "닉네임 수정", description = "닉네임을 변경하고, 변경된 개인정보 블록을 반환합니다.",
            security = { @SecurityRequirement(name = "Authorization"), @SecurityRequirement(name = "Reauth") },
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi...")),
                    @Parameter(name = HEADER, in = ParameterIn.HEADER, required = true,
                            description = "reauthToken (비밀번호 검증으로 발급)",
                            examples = @ExampleObject(value = "reauth_0d2d5f..."))
            })
    public ResponseEntity<ApiResponseDto<SettingsPageResponse>> updateNickname(
            @Valid @RequestBody NicknameUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        SettingsPageResponse data = service.updateNickname(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "닉네임 변경에 성공했습니다.", data));
    }

    /* =========================
     *  전화번호 수정 (수정 후 개인정보 블록 반환)
     * ========================= */
    @ReauthRequired
    @PatchMapping("/phone")
    @Operation(summary = "전화번호 변경", description = "전화번호를 변경하고, 변경된 개인정보 블록을 반환합니다.",
            security = { @SecurityRequirement(name = "Authorization"), @SecurityRequirement(name = "Reauth") },
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi...")),
                    @Parameter(name = HEADER, in = ParameterIn.HEADER, required = true,
                            description = "reauthToken (비밀번호 검증으로 발급)",
                            examples = @ExampleObject(value = "reauth_0d2d5f..."))
            })
    public ResponseEntity<ApiResponseDto<SettingsPageResponse>> updatePhone(
            @Valid @RequestBody PhoneUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        SettingsPageResponse data = service.updatePhone(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "전화번호 변경에 성공했습니다.", data));
    }
    // TODO : 비밀번호 변경

    /* =========================
     *  회원 탈퇴 (data=null 유지)
     * ========================= */
    @ReauthRequired
    @DeleteMapping
    @Operation(summary = "회원 탈퇴",
            security = { @SecurityRequirement(name = "Authorization"), @SecurityRequirement(name = "Reauth") },
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi...")),
                    @Parameter(name = HEADER, in = ParameterIn.HEADER, required = true,
                            description = "reauthToken (비밀번호 검증으로 발급)",
                            examples = @ExampleObject(value = "reauth_0d2d5f..."))
            })
    public ResponseEntity<ApiResponseDto<Void>> deleteMe(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        service.deleteMe(me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "회원 탈퇴에 성공했습니다.", null));
    }
}