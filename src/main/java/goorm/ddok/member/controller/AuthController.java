package goorm.ddok.member.controller;


import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.util.sentry.SentryUserContextService;
import goorm.ddok.member.dto.request.*;
import goorm.ddok.member.dto.response.*;
import goorm.ddok.member.service.AuthService;
import goorm.ddok.member.service.EmailVerificationService;
import goorm.ddok.member.service.PhoneVerificationService;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "회원가입, 로그인, 비밀번호 재설정 등 사용자 인증 API")
public class AuthController {

    private final AuthService authService;
    private final PhoneVerificationService phoneVerificationService;
    private final SentryUserContextService sentryUserContextService;
    private final EmailVerificationService emailVerificationService;


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

    @Operation(
            summary = "전화번호 인증"
    )
    @PostMapping("/phone/send-code")
    public ResponseEntity<ApiResponseDto<PhoneVerificationResponse>> sendCode(
            @Valid @RequestBody PhoneVerificationRequest request) {

        int time = phoneVerificationService.sendVerificationCode(
                request.getPhoneNumber(),
                request.getUsername(),
                request.getAuthType()
        );

        PhoneVerificationResponse expiresIn = new PhoneVerificationResponse(time);

        return ResponseEntity.ok(ApiResponseDto.of(200, "인증번호가 발송되었습니다.", expiresIn));
    }

    @Operation(
            summary = "전화번호 인증번호 확인"
    )
    @PostMapping("/phone/verify-code")
    public ResponseEntity<ApiResponseDto<PhoneVerifyCodeResponse>> verifyCode(
            @Valid @RequestBody PhoneVerifyCodeRequest request) {

        boolean verificationResult = phoneVerificationService.verifyCode(request.getPhoneNumber(), request.getPhoneCode());

        PhoneVerifyCodeResponse verificationResponse = new PhoneVerifyCodeResponse(verificationResult);

        return ResponseEntity.ok(ApiResponseDto.of(200, "인증에 성공했습니다.", verificationResponse));
    }

    @Operation(
            summary = "이메일 인증"
    )
    @GetMapping("/email/send-code")
    public RedirectView verifyEmail(@RequestParam String code) {
        boolean result = emailVerificationService.verifyEmailCode(code);
        String email = emailVerificationService.findVerifiedEmailByCode(code);

        if (result) {
            authService.setEmailVerificationService(email);
        }

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:5173/sign-in");
        return redirectView;
    }

    @Operation(summary = "로그인")
    @PostMapping("/signin")
    public ResponseEntity<ApiResponseDto<SignInResponse>> signIn(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletResponse servletResponse
    ) {
        // 1. 서비스에서 로그인 + 토큰 2개 발급
        SignInResponse response = authService.signIn(signInRequest, servletResponse);

        // ★ Sentry Scope에 유저 정보 세팅
        sentryUserContextService.setCurrentUserContext();

        // ★ Sentry 메시지로 로그인 이벤트 기록
        Sentry.captureMessage(
                "로그인: " + response.getUser().getUsername() + ": " + response.getUser().getNickname(),
                SentryLevel.INFO
        );

        // response(본문)는 AccessToken만, RefreshToken은 쿠키로 헤더에 내려감!
        return ResponseEntity.ok(ApiResponseDto.of(200, "로그인에 성공했습니다.", response));
    }

    @Operation(summary = "로그아웃", security = @SecurityRequirement(name = "Authorization"))
    @PostMapping("/signout")
    public ResponseEntity<ApiResponseDto<Void>> signOut(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletResponse response
    ) {
        authService.signOut(authorizationHeader, response);

        // 1. 토큰에서 유저 정보 추출해서 Sentry 컨텍스트 세팅 & username 반환
        String usernameWithNickname = sentryUserContextService.setUserContextFromToken(authorizationHeader);

        // 2. 누가 로그아웃했는지 메시지 기록
        Sentry.captureMessage("로그아웃: " + usernameWithNickname, SentryLevel.INFO);

        // 3. Sentry Scope에서 유저 정보 제거
        sentryUserContextService.clearUserContext();

        return ResponseEntity.ok(ApiResponseDto.of(200, "로그아웃 되었습니다.", null));
    }

    @Operation(summary = "이메일(아이디) 찾기")
    @PostMapping("/email/find")
    public ResponseEntity<ApiResponseDto<FindEmailResponse>> findEmail(@Valid @RequestBody FindEmailRequest request) {
        FindEmailResponse response = new FindEmailResponse(authService.findEmail(request));
        return ResponseEntity.ok(ApiResponseDto.of(200, "이메일(아이디)을 찾았습니다.", response));
    }

    @Operation(summary = "비밀번호 변경 전 본인 인증")
    @PostMapping("/password/verify-user")
    public ResponseEntity<ApiResponseDto<PasswordVerifyUserResponse>> verifyUser(@Valid @RequestBody PasswordVerifyUserRequest request) {
        String reauthToken = authService.passwordVerifyUser(request);
        PasswordVerifyUserResponse response = new PasswordVerifyUserResponse(reauthToken);
        return ResponseEntity.ok(ApiResponseDto.of(200, "본인 인증에 성공했습니다.", response));
    }

    @Operation(summary = "비밀번호 재설정", security = @SecurityRequirement(name = "Authorization"))
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        authService.verifyAndResetPassword(request, authorizationHeader);
        return ResponseEntity.ok(ApiResponseDto.of(200, "비밀번호가 재설정되었습니다.", null));
    }
}
