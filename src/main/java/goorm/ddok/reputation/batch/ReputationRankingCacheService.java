package goorm.ddok.reputation.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.reputation.dto.response.TemperatureRankResponse;
import goorm.ddok.reputation.dto.response.TemperatureRegionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static goorm.ddok.reputation.cache.ReputationRankRedisKeys.*;

@Service
@RequiredArgsConstructor
public class ReputationRankingCacheService {

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    public TemperatureRankResponse getCachedTop1() {
        String json = redis.opsForValue().get(TOP1_LIVE);
        if (json == null) throw new GlobalException(ErrorCode.RANKING_NOT_READY);
        try {
            return om.readValue(json, TemperatureRankResponse.class);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.JSON_PARSE_ERROR);
        }
    }

    /** 널 허용 조회 (컨트롤러에서 200+null 정책 유지용) */
    public TemperatureRankResponse peekCachedTop1() {
        String json = redis.opsForValue().get(TOP1_LIVE);
        if (json == null) return null;
        try {
            return om.readValue(json, TemperatureRankResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    public List<TemperatureRankResponse> getCachedTop10() {
        String json = redis.opsForValue().get(TOP10_LIVE);
        if (json == null) throw new GlobalException(ErrorCode.RANKING_NOT_READY);
        try {
            return om.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.JSON_PARSE_ERROR);
        }
    }

    public List<TemperatureRegionResponse> getCachedRegionTop1() {
        String json = redis.opsForValue().get(REGION_TOP1_LIVE);
        if (json == null) throw new GlobalException(ErrorCode.RANKING_NOT_READY);
        try {
            return om.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.JSON_PARSE_ERROR);
        }
    }
}
