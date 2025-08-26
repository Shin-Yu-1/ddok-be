package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.global.util.sentry.SentryUserContextService;
import goorm.ddok.member.dto.request.*;
import goorm.ddok.member.dto.response.*;
import goorm.ddok.member.service.AuthService;
import goorm.ddok.member.service.EmailVerificationService;
import goorm.ddok.member.service.PhoneVerificationService;
import goorm.ddok.member.service.TokenService;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "회원가입, 로그인, 비밀번호 재설정 등 사용자 인증 API")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final PhoneVerificationService phoneVerificationService;
    private final SentryUserContextService sentryUserContextService;
    private final EmailVerificationService emailVerificationService;

    @Operation(
            summary = "회원가입",
            description = "사용자 회원가입을 수행합니다. 이메일 인증 메일이 발송됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignUpRequest.class),
                            examples = @ExampleObject(name = "요청 예시", value = """
                {
                  "email": "test@test.com",
                  "username": "홍길동",
                  "password": "1Q2w3e4r!@#",
                  "passwordCheck": "1Q2w3e4r!@#",
                  "phoneNumber": "01012345678",
                  "phoneCode": "828282"
                }
                """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 201, "message": "회원가입이 완료되었습니다.", "data": { "id": 1, "username": "홍길동" } }
                """))),
            @ApiResponse(responseCode = "400", description = "입력값 오류 (이름/비밀번호/전화번호 형식 등)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 400, "message": "비밀번호는 8자 이상이어야 합니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "409", description = "중복 이메일/전화번호",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 409, "message": "이미 가입된 이메일입니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        SignUpResponse response = authService.signup(signUpRequest);
        return ResponseEntity.ok(ApiResponseDto.of(201, "회원가입이 완료되었습니다.", response));
    }

    @Operation(
            summary = "이메일 중복 확인",
            description = "입력한 이메일로 가입 가능 여부를 확인합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EmailCheckRequest.class),
                            examples = @ExampleObject(value = """
                { "email": "test@test.com" }
                """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 가능한 이메일",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 200, "message": "사용 가능한 이메일입니다.", "data": { "IsAvailable": true } }
                """))),
            @ApiResponse(responseCode = "409", description = "중복 이메일",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 409, "message": "이미 가입된 이메일입니다.", "data": null }
                """)))
    })
    @PostMapping("/email/check")
    public ResponseEntity<ApiResponseDto<EmailCheckResponse>> checkEmail(
            @Valid @RequestBody EmailCheckRequest emailCheckRequest) {
        boolean isAvailable = authService.isEmailAlreadyExist(emailCheckRequest.getEmail());
        EmailCheckResponse emailCheckResponse = new EmailCheckResponse(isAvailable);
        return ResponseEntity.ok(ApiResponseDto.of(200, "사용 가능한 이메일입니다.", emailCheckResponse));
    }

    @Operation(
            summary = "전화번호 인증 코드 발송",
            description = "입력한 전화번호로 6자리 인증번호를 발송합니다. (유효시간 초 단위 반환)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PhoneVerificationRequest.class),
                            examples = @ExampleObject(value = """
                { "phoneNumber": "01012345678", "username": "홍길동", "authType": "SIGN_UP" }
                """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "발송 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 200, "message": "인증번호가 발송되었습니다.", "data": { "expiresIn": 60 } }
                """))),
            @ApiResponse(responseCode = "400", description = "전화번호 형식 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 400, "message": "올바른 휴대전화 번호 형식이 아닙니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "409", description = "중복 이름+전화번호 (회원가입 중)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 409, "message": "해당 이름과 연락처로 이미 가입된 회원이 있습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "500", description = "SMS 발송 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 500, "message": "SMS 발송 실패", "data": null }
                """)))
    })
    @PostMapping("/phone/send-code")
    public ResponseEntity<ApiResponseDto<PhoneVerificationResponse>> sendCode(
            @Valid @RequestBody PhoneVerificationRequest request) {
        int time = phoneVerificationService.sendVerificationCode(
                request.getPhoneNumber(), request.getUsername(), request.getAuthType());
        PhoneVerificationResponse expiresIn = new PhoneVerificationResponse(time);
        return ResponseEntity.ok(ApiResponseDto.of(200, "인증번호가 발송되었습니다.", expiresIn));
    }

    @Operation(
            summary = "전화번호 인증번호 확인",
            description = "수신한 인증번호를 검증합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PhoneVerifyCodeRequest.class),
                            examples = @ExampleObject(value = """
                { "phoneNumber": "01012345678", "phoneCode": "123456" }
                """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 200, "message": "인증에 성공했습니다.", "data": { "verified": true } }
                """))),
            @ApiResponse(responseCode = "400", description = "인증번호 만료",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 400, "message": "인증번호가 만료되었습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "404", description = "인증 요청 기록 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 404, "message": "인증 요청 기록이 없습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "409", description = "이미 인증 완료된 요청",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 409, "message": "이미 인증이 완료된 계정입니다.", "data": null }
                """)))
    })
    @PostMapping("/phone/verify-code")
    public ResponseEntity<ApiResponseDto<PhoneVerifyCodeResponse>> verifyCode(
            @Valid @RequestBody PhoneVerifyCodeRequest request) {
        boolean verificationResult = phoneVerificationService.verifyCode(request.getPhoneNumber(), request.getPhoneCode());
        PhoneVerifyCodeResponse verificationResponse = new PhoneVerifyCodeResponse(verificationResult);
        return ResponseEntity.ok(ApiResponseDto.of(200, "인증에 성공했습니다.", verificationResponse));
    }

    @Operation(
            summary = "이메일 인증 처리(리다이렉트)",
            description = "이메일로 전달된 인증 링크를 통해 인증을 완료합니다. 완료 후 프론트엔드로 리다이렉트됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "리다이렉트 성공 (프론트 페이지로 이동)"),
            @ApiResponse(responseCode = "400", description = "잘못되었거나 만료된 코드",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 미완료 / 재발송됨",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @GetMapping("/email/send-code")
    public RedirectView verifyEmail(
            @Parameter(name = "code", description = "이메일 인증 코드", required = true, in = ParameterIn.QUERY,
                    examples = @ExampleObject(value = "a7b9c-...-uuid"))
            @RequestParam String code
    ) {
        boolean result = emailVerificationService.verifyEmailCode(code);
        String email = emailVerificationService.findVerifiedEmailByCode(code);
        if (result) authService.setEmailVerificationService(email);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:5173/sign-in");
        return redirectView;
    }

    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호로 로그인합니다. Access Token은 본문에, Refresh Token은 HttpOnly 쿠키로 반환됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignInRequest.class),
                            examples = @ExampleObject(value = """
                { "email": "test@test.com", "password": "1Q2w3e4r!@#" }
                """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 200,
                  "message": "로그인에 성공했습니다.",
                  "data": {
                    "accessToken": "eyJhbGciOi...",
                    "user": {
                      "id": 1, "username": "홍길동", "email": "test@test.com",
                      "nickname": "고통스러운 개발자", "profileImageUrl": "https://...",
                      "mainPosition": "백엔드", "IsPreference": true,
                      "location": { "latitude": 37.5665, "longitude": 126.9780, "address": "서울..." }
                    }
                  }
                }
                """))),
            @ApiResponse(responseCode = "401", description = "아이디/비밀번호 불일치 또는 이메일 미인증/재발송",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/signin")
    public ResponseEntity<ApiResponseDto<SignInResponse>> signIn(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletResponse servletResponse
    ) {
        SignInResponse response = authService.signIn(signInRequest, servletResponse);
        sentryUserContextService.setCurrentUserContext();
        Sentry.captureMessage(
                "로그인: " + response.getUser().getUsername() + ": " + response.getUser().getNickname(),
                SentryLevel.INFO
        );
        return ResponseEntity.ok(ApiResponseDto.of(200, "로그인에 성공했습니다.", response));
    }

    @Operation(
            summary = "로그아웃",
            description = "Access Token 유효성을 검증하고 서버 저장 Refresh Token을 삭제합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {accessToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 200, "message": "로그아웃 되었습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "401", description = "토큰 누락/유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/signout")
    public ResponseEntity<ApiResponseDto<Void>> signOut(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletResponse response
    ) {
        authService.signOut(authorizationHeader, response);
        String usernameWithNickname = sentryUserContextService.setUserContextFromToken(authorizationHeader);
        Sentry.captureMessage("로그아웃: " + usernameWithNickname, SentryLevel.INFO);
        sentryUserContextService.clearUserContext();
        return ResponseEntity.ok(ApiResponseDto.of(200, "로그아웃 되었습니다.", null));
    }

    @Operation(
            summary = "이메일(아이디) 찾기",
            description = "휴대폰 인증(코드 검증) 후 이름+전화번호로 가입 이메일을 조회합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = FindEmailRequest.class),
                            examples = @ExampleObject(value = """
                { "username": "홍길동", "phoneNumber": "01012345678", "phoneCode": "012345" }
                """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 200, "message": "이메일(아이디)을 찾았습니다.", "data": { "email": "test@test.com" } }
                """))),
            @ApiResponse(responseCode = "404", description = "인증기록 없음 / 사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "해당 인증 요청이 이미 검증됨",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/email/find")
    public ResponseEntity<ApiResponseDto<FindEmailResponse>> findEmail(@Valid @RequestBody FindEmailRequest request) {
        FindEmailResponse response = new FindEmailResponse(authService.findEmail(request));
        return ResponseEntity.ok(ApiResponseDto.of(200, "이메일(아이디)을 찾았습니다.", response));
    }

    @Operation(
            summary = "비밀번호 변경 전 본인 인증",
            description = "이름/이메일/전화번호/인증코드 검증 후 재인증 토큰(reauthToken)을 발급합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PasswordVerifyUserRequest.class),
                            examples = @ExampleObject(value = """
                {
                  "username": "홍길동",
                  "email": "test@test.com",
                  "phoneNumber": "01012345678",
                  "phoneCode": "012345"
                }
                """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "본인 인증 성공 (reauthToken 발급)",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 200, "message": "본인 인증에 성공했습니다.", "data": { "reauthToken": "REAUTH_TOKEN_VALUE" } }
                """))),
            @ApiResponse(responseCode = "404", description = "인증기록 없음 / 사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "이미 인증된 요청",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/password/verify-user")
    public ResponseEntity<ApiResponseDto<PasswordVerifyUserResponse>> verifyUser(@Valid @RequestBody PasswordVerifyUserRequest request) {
        String reauthToken = authService.passwordVerifyUser(request);
        PasswordVerifyUserResponse response = new PasswordVerifyUserResponse(reauthToken);
        return ResponseEntity.ok(ApiResponseDto.of(200, "본인 인증에 성공했습니다.", response));
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = "발급받은 reauthToken(Bearer)으로 검증 후 새 비밀번호로 재설정합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            parameters = {
                    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true,
                            description = "Bearer {reauthToken}",
                            examples = @ExampleObject(value = "Bearer eyJhbGciOi..."))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PasswordResetRequest.class),
                            examples = @ExampleObject(value = """
                { "newPassword": "!@#123qweR", "passwordCheck": "!@#123qweR" }
                """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재설정 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 200, "message": "비밀번호가 재설정되었습니다.", "data": null }
                """))),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치 등 입력 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "토큰 누락/유효하지 않음/검증 미완료",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "인증 기록 또는 사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        authService.verifyAndResetPassword(request, authorizationHeader);
        return ResponseEntity.ok(ApiResponseDto.of(200, "비밀번호가 재설정되었습니다.", null));
    }

    @Operation(
            summary = "AccessToken 재발급",
            description = "HttpOnly 쿠키에 담긴 Refresh Token을 검증하고 새로운 Access Token을 발급합니다.",
            parameters = {
                    @Parameter(name = "refreshToken", in = ParameterIn.COOKIE, required = true,
                            description = "HttpOnly Refresh Token 쿠키",
                            examples = @ExampleObject(value = "eyJhbGciOi..."))
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                { "status": 200, "message": "토큰 재발급에 성공했습니다.", "data": { "accessToken": "eyJhbGciOi..." } }
                """))),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 유효하지 않음/만료",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자/토큰 정보 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/token")
    public ResponseEntity<ApiResponseDto<TokenResponse>> reissueAccessToken(
            @CookieValue("refreshToken") String refreshToken
    ) {
        String result = tokenService.reissueAccessToken(refreshToken);
        TokenResponse response = new TokenResponse(result);
        return ResponseEntity.ok(ApiResponseDto.of(200, "토큰 재발급에 성공했습니다.", response));
    }

    @Operation(
            summary = "개인화 설정 생성",
            description = "대표/관심 포지션, 기술스택, 위치, 특성, 생년월일, 활동시간을 등록합니다.",
            security = @SecurityRequirement(name = "Authorization"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PreferenceRequest.class),
                            examples = @ExampleObject(value = """
                {
                  "mainPosition": "백엔드",
                  "subPosition": ["프론트엔드", "데브옵스"],
                  "techStacks": ["Java", "Spring Boot", "React"],
                  "location": { "latitude": 37.5665, "longitude": 126.9780, "address": "서울..." },
                  "traits": ["협업", "문제 해결"],
                  "birthDate": "1997-10-10",
                  "activeHours": { "start": "19", "end": "23" }
                }
                """)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                {
                  "status": 201,
                  "message": "개인화 설정이 완료되었습니다.",
                  "data": {
                    "id": 1,
                    "username": "홍길동",
                    "email": "test@test.com",
                    "mainPosition": "백엔드",
                    "profileImageUrl": "https://...",
                    "nickname": "백엔드_호랑이",
                    "isPreferences": true,
                    "preferences": {
                      "mainPosition": "백엔드",
                      "subPosition": ["프론트엔드", "데브옵스"],
                      "techStacks": ["Java", "Spring Boot", "React"],
                      "location": { "latitude": 37.5665, "longitude": 126.9780, "address": "서울..." },
                      "traits": ["협업", "문제 해결"],
                      "birthDate": "1997-10-10",
                      "activeHours": { "start": "19", "end": "23" }
                    }
                  }
                }
                """))),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))
    })
    @PostMapping("/preferences")
    public ResponseEntity<ApiResponseDto<PreferenceResponse>> createPreference(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody PreferenceRequest request
    ) {
        PreferenceResponse response = authService.createPreference(user.getId(), request);
        return ResponseEntity.ok(ApiResponseDto.of(201, "개인화 설정이 완료되었습니다.", response));
    }
}
