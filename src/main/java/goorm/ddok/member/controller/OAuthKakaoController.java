package goorm.ddok.member.controller;

import goorm.ddok.global.security.jwt.JwtTokenProvider;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.dto.request.KakaoSignInRequest;
import goorm.ddok.member.service.KakaoOAuthService;
import goorm.ddok.member.service.SocialAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class OAuthKakaoController {

    private final KakaoOAuthService kakaoOAuthService;
    private final SocialAuthService socialAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    // ★ 리다이렉트 URI를 프로퍼티로 관리 (테스트 환경에 맞춰 바꾸기 쉬움)
    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUriProp;

    // ★ scope도 프로퍼티로 관리(기본값 닉네임만)
    @Value("${oauth.kakao.scope:profile_nickname}")
    private String scope;

    /** 백엔드 단독 테스트: 카카오 동의화면으로 리다이렉트 */
    @GetMapping("/oauth/kakao")
    public void redirectToKakao(HttpServletResponse res) throws Exception {
        String encodedRedirect = URLEncoder.encode(redirectUriProp, StandardCharsets.UTF_8);
        String url = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + encodedRedirect
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);
        res.sendRedirect(url);
    }

    /** 콜백 */
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code) {
        // ★ 교환시에도 "인가 때 사용한 redirectUri와 완전히 같은 값"을 사용
        String kakaoAccessToken = kakaoOAuthService.exchangeCodeForAccessToken(code, redirectUriProp);
        var kuser = kakaoOAuthService.fetchUser(kakaoAccessToken);
        User user = socialAuthService.upsertKakaoUser(kuser.kakaoId(), kuser.email(), kuser.nickname(), kuser.profileImageUrl());

        String accessToken = jwtTokenProvider.createToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "카카오 로그인에 성공했습니다.",
                "data", Map.of(
                        "accessToken", accessToken,
                        "user", Map.of(
                                "id", user.getId(),
                                "username", user.getUsername(),
                                "email", user.getEmail(),
                                "nickname", user.getNickname(),
                                "profileImageUrl", null,
                                "isPreferences", true,
                                "location", null
                        )
                )
        ));
    }

    /** 명세형 API */
    @PostMapping("/api/auth/signin/kakao")
    public ResponseEntity<?> signInKakao(@Valid @RequestBody KakaoSignInRequest req) {
        // ★ 여기서는 "클라이언트가 인가 때 사용한 redirectUri"를 그대로 받아 사용
        String kakaoAccessToken = kakaoOAuthService.exchangeCodeForAccessToken(req.authorizationCode(), req.redirectUri());
        var kuser = kakaoOAuthService.fetchUser(kakaoAccessToken);
        User user = socialAuthService.upsertKakaoUser(kuser.kakaoId(), kuser.email(), kuser.nickname(), kuser.profileImageUrl());

        String accessToken = jwtTokenProvider.createToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "로그인에 성공했습니다.",
                "data", Map.of(
                        "accessToken", accessToken,
                        "user", Map.of(
                                "id", user.getId(),
                                "username", user.getUsername(),
                                "email", user.getEmail(),
                                "nickname", user.getNickname(),
                                "profileImageUrl", null,
                                "isPreferences", true,
                                "location", null
                        )
                )
        ));
    }
}