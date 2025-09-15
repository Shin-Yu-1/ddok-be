package goorm.ddok.reputation.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.ddok.reputation.dto.response.TemperatureRankResponse;
import goorm.ddok.reputation.dto.response.TemperatureRegionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static goorm.ddok.reputation.cache.ReputationRankRedisKeys.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReputationRankingWriter {

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    public void writeTop1(TemperatureRankResponse top1, Instant now) {
        try {
            if (top1 == null) {
                redis.delete(TOP1_LIVE);
                return;
            }
            String json = om.writeValueAsString(top1);
            redis.opsForValue().set(TOP1_TMP, json, Duration.ofSeconds(DEFAULT_TTL_SECONDS));
            Objects.requireNonNull(redis.getConnectionFactory()).getConnection()
                    .rename(TOP1_TMP.getBytes(), TOP1_LIVE.getBytes());
        } catch (Exception e) {
            log.error("Failed to write TOP1 to Redis", e);
        }
    }

    public void writeTop10(List<TemperatureRankResponse> top10, Instant now) {
        try {
            List<TemperatureRankResponse> withUpdated =
                    IntStream.range(0, top10.size())
                            .mapToObj(i -> top10.get(i).toBuilder()
                                    .rank(i + 1)
                                    .IsMine(false)
                                    .dmRequestPending(false)
                                    .chatRoomId(null)
                                    .updatedAt(now)
                                    .build())
                            .toList();

            String json = om.writeValueAsString(withUpdated);
            redis.opsForValue().set(TOP10_TMP, json, Duration.ofSeconds(DEFAULT_TTL_SECONDS));
            Objects.requireNonNull(redis.getConnectionFactory()).getConnection()
                    .rename(TOP10_TMP.getBytes(), TOP10_LIVE.getBytes());
        } catch (Exception e) {
            log.error("Failed to write TOP10 to Redis", e);
        }
    }

    public void writeRegionTop1(List<TemperatureRegionResponse> regionTop1, Instant now) {
        try {
            List<TemperatureRegionResponse> withUpdated = regionTop1.stream()
                    .map(r -> r.toBuilder()
                            .dmRequestPending(false)
                            .chatRoomId(null)
                            .IsMine(false)
                            .updatedAt(now)
                            .build())
                    .toList();

            String json = om.writeValueAsString(withUpdated);
            redis.opsForValue().set(REGION_TOP1_TMP, json, Duration.ofSeconds(DEFAULT_TTL_SECONDS));
            Objects.requireNonNull(redis.getConnectionFactory()).getConnection()
                    .rename(REGION_TOP1_TMP.getBytes(), REGION_TOP1_LIVE.getBytes());
        } catch (Exception e) {
            log.error("Failed to write REGION_TOP1 to Redis", e);
        }
    }
}