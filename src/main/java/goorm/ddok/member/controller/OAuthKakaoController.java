package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.util.sentry.SentryUserContextService;
import goorm.ddok.member.dto.request.KakaoSignInRequest;
import goorm.ddok.member.dto.response.SignInResponse;
import goorm.ddok.member.service.SocialSignInService;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class OAuthKakaoController {

    private final SocialSignInService socialSignInService;
    private final SentryUserContextService sentryUserContextService;

    // 카카오 콘솔 값 (application-env.properties에 존재해야 함)
    @Value("${oauth.kakao.client-id}")
    private String clientId;

    // 인가/토큰교환 양쪽에서 "동일 값" 사용해야 함
    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUriProp;

    // 필요 scope (추가 동의가 막혀있으면 기본값 profile_nickname만)
    @Value("${oauth.kakao.scope:profile_nickname}")
    private String scope;

    @GetMapping("/oauth/kakao")
    public void redirectToKakao(HttpServletResponse res) throws Exception {
        String encodedRedirect = URLEncoder.encode(redirectUriProp, StandardCharsets.UTF_8);
        String encodedScope    = URLEncoder.encode(scope, StandardCharsets.UTF_8);

        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + encodedRedirect
                + "&scope=" + encodedScope;

        res.sendRedirect(url);
    }

    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponseDto<SignInResponse>> kakaoCallback(
            @RequestParam("code") String code,
            HttpServletResponse servletResponse
    ) {
        SignInResponse response = socialSignInService.signInWithKakaoCode(code, redirectUriProp, servletResponse);

        // Sentry 컨텍스트 및 로깅 (일반 로그인과 동일)
        sentryUserContextService.setCurrentUserContext();
        Sentry.captureMessage(
                "소셜 로그인(KAKAO/Callback): " + response.getUser().getUsername() + ": " + response.getUser().getNickname(),
                SentryLevel.INFO
        );

        return ResponseEntity.ok(ApiResponseDto.of(200, "카카오 로그인에 성공했습니다.", response));
    }

    @PostMapping("/api/auth/signin/kakao")
    public ResponseEntity<ApiResponseDto<SignInResponse>> signInKakao(
            @Valid @RequestBody KakaoSignInRequest req,
            HttpServletResponse servletResponse
    ) {
        SignInResponse response = socialSignInService.signInWithKakaoCode(
                req.authorizationCode(), req.redirectUri(), servletResponse
        );

        sentryUserContextService.setCurrentUserContext();
        Sentry.captureMessage(
                "소셜 로그인(KAKAO): " + response.getUser().getUsername() + ": " + response.getUser().getNickname(),
                SentryLevel.INFO
        );

        return ResponseEntity.ok(ApiResponseDto.of(200, "카카오 로그인에 성공했습니다.", response));
    }
}