package goorm.ddok.member.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.jwt.JwtTokenProvider;
import goorm.ddok.global.security.token.RefreshTokenService;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public String reissueAccessToken(String refreshToken) {
        // 1. 토큰 유효성 검증 (예외 기반)
        jwtTokenProvider.validateToken(refreshToken); // 예외 발생 시 자동 중단

        // 2. 토큰에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 3. Redis에 저장된 리프레시 토큰과 비교
        String savedToken = refreshTokenService.find(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (!savedToken.equals(refreshToken)) {
            throw new GlobalException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 4. 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        // 5. 새 Access Token 발급
        return jwtTokenProvider.createToken(user.getId());
    }
}
