package goorm.ddok.member.controller;


import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.member.dto.request.EmailCheckRequest;
import goorm.ddok.member.dto.request.SignUpRequest;
import goorm.ddok.member.dto.response.EmailCheckResponse;
import goorm.ddok.member.dto.response.SignUpResponse;
import goorm.ddok.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "회원가입, 로그인, 비밀번호 재설정 등 사용자 인증 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        SignUpResponse response = authService.signup(signUpRequest);
        return ResponseEntity.ok(ApiResponseDto.of(201, "회원가입이 완료되었습니다.", response));
    }

    @Operation(summary = "이메일 중복 확인")
    @PostMapping("/email/check")
    public ResponseEntity<ApiResponseDto<EmailCheckResponse>> checkEmail(
            @Valid @RequestBody EmailCheckRequest emailCheckRequest) {
        boolean isAvailable = authService.isEmailAlreadyExist(emailCheckRequest.getEmail());

        EmailCheckResponse emailCheckResponse = new EmailCheckResponse(isAvailable);

        return ResponseEntity.ok(ApiResponseDto.of(
                200, "사용 가능한 이메일입니다.", emailCheckResponse
        ));
    }
}
