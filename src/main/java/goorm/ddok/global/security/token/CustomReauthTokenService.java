package goorm.ddok.global.security.token;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomReauthTokenService {

    private static final String KEY_PREFIX = "reauth:";
    private final StringRedisTemplate redis;
    private final Duration ttl = Duration.ofMinutes(5);

    public String issue(Long userId) {
        String token = "reauth_" + UUID.randomUUID().toString().replace("-", "");
        String key = KEY_PREFIX + token;
        redis.opsForValue().set(key, String.valueOf(userId), ttl);
        return token;
    }

    public void validate(String token, Long expectedUserId) {
        if (token == null || token.isBlank()) {
            throw new GlobalException(ErrorCode.REAUTH_REQUIRED);
        }
        String key = KEY_PREFIX + token;
        String val = redis.opsForValue().get(key);
        if (val == null) {
            throw new GlobalException(ErrorCode.INVALID_REAUTH_TOKEN);
        }
        if (!val.equals(String.valueOf(expectedUserId))) {
            throw new GlobalException(ErrorCode.REAUTH_USER_MISMATCH);
        }
    }
}
