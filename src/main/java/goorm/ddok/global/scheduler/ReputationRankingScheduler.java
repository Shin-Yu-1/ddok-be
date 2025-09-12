package goorm.ddok.global.scheduler;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.domain.User;
import goorm.ddok.reputation.dto.response.TemperatureRankResponse;
import goorm.ddok.reputation.dto.response.TemperatureRegionResponse;
import goorm.ddok.reputation.service.ReputationQueryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReputationRankingScheduler {

    private final ReputationQueryService reputationQueryService;

    private final AtomicReference<TemperatureRankResponse> top1Cache = new AtomicReference<>();
    private final AtomicReference<List<TemperatureRankResponse>> top10Cache = new AtomicReference<>();
    private final AtomicReference<List<TemperatureRegionResponse>> regionTop1Cache = new AtomicReference<>();

    private final AtomicReference<Instant> lastUpdated = new AtomicReference<>();

    /** 서버 시작 시 바로 1회 실행 */
    @PostConstruct
    public void init() {
        updateTop1Ranking();
        updateTop10Ranking();
        updateRegionTop1Ranking();
    }

    /** 매 시간마다 TOP1 + TOP10 갱신 */
    @Scheduled(cron = "0 0 * * * *")
    public void updateRankings() {
        updateTop1Ranking();
        updateTop10Ranking();
        updateRegionTop1Ranking();
    }

    public void updateTop1Ranking() {
        try {
            // currentUser == null
            TemperatureRankResponse top1 = reputationQueryService.getTop1TemperatureRank(null);
            Instant now = Instant.now();

            // 캐시에 저장 (updatedAt 덮어쓰기)
            TemperatureRankResponse withUpdatedAt = TemperatureRankResponse.builder()
                    .rank(top1.getRank())
                    .userId(top1.getUserId())
                    .nickname(top1.getNickname())
                    .temperature(top1.getTemperature())
                    .mainPosition(top1.getMainPosition())
                    .profileImageUrl(top1.getProfileImageUrl())
                    .chatRoomId(top1.getChatRoomId())
                    .dmRequestPending(top1.isDmRequestPending())
                    .IsMine(false)
                    .mainBadge(top1.getMainBadge())
                    .abandonBadge(top1.getAbandonBadge())
                    .updatedAt(now)
                    .build();

            top1Cache.set(withUpdatedAt);
            lastUpdated.set(now);

            log.info("✅ TOP1 랭킹 갱신 완료: userId={}, temp={}, updatedAt={}",
                    top1.getUserId(), top1.getTemperature(), lastUpdated.get());
        } catch (Exception e) {
            log.error("❌ TOP1 랭킹 갱신 실패", e);
        }
    }

    private void updateTop10Ranking() {
        try {
            List<TemperatureRankResponse> top10 = reputationQueryService.getTop10TemperatureRank(null);
            Instant now = Instant.now();

            // 각 유저 순위, updatedAt 덮어쓰기
            List<TemperatureRankResponse> withUpdatedAt =
                    IntStream.range(0, top10.size())
                            .mapToObj(i -> top10.get(i).toBuilder()
                                    .rank(i + 1)
                                    .IsMine(false)
                                    .dmRequestPending(false)
                                    .chatRoomId(null)
                                    .updatedAt(now)
                                    .build()
                            )
                            .collect(Collectors.toList());

            top10Cache.set(withUpdatedAt);
            lastUpdated.set(now);

            log.info("✅ TOP10 랭킹 갱신 완료, updatedAt={}", now);
        } catch (Exception e) {
            log.error("❌ TOP10 랭킹 갱신 실패", e);
        }
    }

    private void updateRegionTop1Ranking() {
        try {
            List<TemperatureRegionResponse> regionTop1 = reputationQueryService.getRegionTop1Rank(null);
            Instant now = Instant.now();

            // updatedAt 덮어쓰기
            List<TemperatureRegionResponse> withUpdatedAt = regionTop1.stream()
                    .map(r -> r.toBuilder()
                            .dmRequestPending(false)
                            .chatRoomId(null)
                            .IsMine(false)
                            .updatedAt(now)
                            .build())
                    .toList();

            regionTop1Cache.set(withUpdatedAt);
            lastUpdated.set(now);

            log.info("✅ 지역별 TOP1 랭킹 갱신 완료, updatedAt={}", now);
        } catch (Exception e) {
            log.error("❌ 지역별 TOP1 랭킹 갱신 실패", e);
        }
    }

    /** 컨트롤러에서 캐시 조회용 */
    public TemperatureRankResponse getCachedTop1() {
        TemperatureRankResponse cached = top1Cache.get();
        if (cached == null) {
            throw new GlobalException(ErrorCode.RANKING_NOT_READY);
        }
        return cached;
    }

    public List<TemperatureRankResponse> getCachedTop10() {
        List<TemperatureRankResponse> cached = top10Cache.get();
        if (cached == null) {
            throw new GlobalException(ErrorCode.RANKING_NOT_READY);
        }
        return cached;
    }

    public List<TemperatureRegionResponse> getCachedRegionTop1() {
        List<TemperatureRegionResponse> cached = regionTop1Cache.get();
        if (cached == null) {
            throw new GlobalException(ErrorCode.RANKING_NOT_READY);
        }
        return cached;
    }
}
