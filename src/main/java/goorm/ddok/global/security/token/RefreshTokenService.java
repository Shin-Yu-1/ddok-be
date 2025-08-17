package goorm.ddok.global.security.token;

import goorm.ddok.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "rt:"; // 네임스페이스 (ex. rt:123)
    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    /** 발급/갱신 */
    public void save(Long userId, String refreshToken) {
        long ttlMs = jwtTokenProvider.getRefreshTokenExpireMillis();
        redisTemplate.opsForValue().set(key(userId), refreshToken, Duration.ofMillis(ttlMs));
    }

    /** 조회 */
    public Optional<String> find(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    /** 저장된 값과 일치하는지 검증(선택) */
    public boolean matches(Long userId, String refreshToken) {
        String saved = redisTemplate.opsForValue().get(key(userId));
        return saved != null && saved.equals(refreshToken);
    }

    /** 삭제 (로그아웃/강제 로그아웃/로테이트) */
    public void delete(Long userId) {
        redisTemplate.delete(key(userId));
    }

    /** 로테이트 */
    public void rotate(Long userId, String newRefreshToken) {
        delete(userId);
        save(userId, newRefreshToken);
    }
}
