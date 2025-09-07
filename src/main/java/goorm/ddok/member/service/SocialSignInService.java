package goorm.ddok.member.service;

import goorm.ddok.global.security.jwt.JwtTokenProvider;
import goorm.ddok.global.security.token.RefreshTokenService;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.dto.response.LocationResponse;
import goorm.ddok.member.dto.response.SignInResponse;
import goorm.ddok.member.dto.response.SignInUserResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialSignInService {

    private final KakaoOAuthService kakaoOAuthService;
    private final SocialAuthService socialAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public SignInResponse signInWithKakaoCode(String authorizationCode,
                                              String redirectUri,
                                              HttpServletResponse response) {

        // 1) Authorization Code → Access Token 교환
        KakaoOAuthService.KakaoTokenResponse tokenRes =
                kakaoOAuthService.exchangeCodeForAccessToken(authorizationCode, redirectUri);

        String kakaoAccessToken = tokenRes.getAccess_token();
        String kakaoRefreshToken = tokenRes.getRefresh_token(); // 카카오 refresh_token (필요 시 저장/재사용)

        // 2) 카카오 사용자 정보 조회
        var kuser = kakaoOAuthService.fetchUser(kakaoAccessToken);

        // 3) User DB upsert
        User user = socialAuthService.upsertKakaoUser(
                kuser.kakaoId(), kuser.email(), kuser.nickname(), kuser.profileImageUrl()
        );

        // 4) JWT 발급
        String accessToken  = jwtTokenProvider.createToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 5) JWT RefreshToken 저장
        refreshTokenService.save(user.getId(), refreshToken);

        // 6) RefreshToken을 HttpOnly 쿠키로 내려줌
        long ttlMs  = jwtTokenProvider.getRefreshTokenExpireMillis();
        long ttlSec = Math.max(1, ttlMs / 1000);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(ttlSec)
                .sameSite("None")
                .build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

        // 7) 위치 정보 매핑
        LocationResponse location =
                Optional.ofNullable(user.getLocation())
                        .map(LocationResponse::from)
                        .orElse(null);

        // 8) 사용자 DTO 생성
        boolean isPreferences = checkIfUserHasPreferences(user);

        boolean isSocial = checkIfUserSocial(user);

        SignInUserResponse userDto = new SignInUserResponse(user, isPreferences, isSocial, location);

        return new SignInResponse(accessToken, userDto);
    }

    private boolean checkIfUserSocial(User user) {
        return user.getEmail() == null || user.getEmail().isEmpty();
    }

    private boolean checkIfUserHasPreferences(User user) {
        return user.getLocation() != null &&
                !user.getPositions().isEmpty() &&
                !user.getTraits().isEmpty() &&
                user.getBirthDate() != null &&
                user.getActivity() != null;
    }
}