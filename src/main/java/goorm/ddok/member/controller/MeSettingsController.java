package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.dto.request.NicknameUpdateRequest;
import goorm.ddok.member.dto.request.PhoneUpdateRequest;
import goorm.ddok.member.dto.request.ProfileImageUpdateRequest;
import goorm.ddok.member.dto.response.SettingsPageResponse;
import goorm.ddok.member.service.MeSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/settings")
@RequiredArgsConstructor
public class MeSettingsController {

    private final MeSettingsService service;

    /* =========================
     *  개인정보변경 페이지 조회
     * ========================= */
    @GetMapping
    @Operation(summary = "개인정보변경 페이지 조회")
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
     *  프로필 이미지 수정 (수정 후 개인정보 블록 반환)
     * ========================= */
    @PatchMapping("/image")
    @Operation(summary = "프로필 이미지 수정",
            description = "요청 바디에 profileImageUrl이 비어있으면 닉네임 기반 기본 이미지를 생성해 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "message": "프로필 이미지 수정에 성공했습니다.",
                          "data": {
                            "userId": 1,
                            "profileImageUrl": "data:image/svg+xml;base64,...",
                            "username": "곽두철",
                            "nickname": "똑똑한 똑똑이",
                            "birthDate": "1997-10-10",
                            "email": "User@email.com",
                            "phoneNumber": "01012345678",
                            "password": "********"
                          }
                        }""")))
    })
    public ResponseEntity<ApiResponseDto<SettingsPageResponse>> updateImage( // [CHANGED] 반환형
                                                                             @Valid @RequestBody ProfileImageUpdateRequest req,
                                                                             @AuthenticationPrincipal CustomUserDetails me
    ) {
        SettingsPageResponse updated = service.updateProfileImage(req, me);   // [CHANGED] 업데이트 후 페이지 응답 반환
        return ResponseEntity.ok(ApiResponseDto.of(200, "프로필 이미지 수정에 성공했습니다.", updated)); // [CHANGED]
    }

    /* =========================
     *  닉네임 수정 (수정 후 개인정보 블록 반환)
     * ========================= */
    @PatchMapping("/nickname")
    @Operation(summary = "닉네임 수정", description = "닉네임을 변경하고, 변경된 개인정보 블록을 반환합니다.")
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
    @PatchMapping("/phone")
    @Operation(summary = "전화번호 변경", description = "전화번호를 변경하고, 변경된 개인정보 블록을 반환합니다.")
    public ResponseEntity<ApiResponseDto<SettingsPageResponse>> updatePhone(
            @Valid @RequestBody PhoneUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        SettingsPageResponse data = service.updatePhone(req, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "전화번호 변경에 성공했습니다.", data));
    }

    /* =========================
     *  회원 탈퇴 (data=null 유지)
     * ========================= */
    @DeleteMapping
    @Operation(summary = "회원 탈퇴")
    public ResponseEntity<ApiResponseDto<Void>> deleteMe(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        service.deleteMe(me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "회원 탈퇴에 성공했습니다.", null));
    }
}