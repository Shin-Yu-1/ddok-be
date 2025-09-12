package goorm.ddok.global.scheduler;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.reputation.dto.response.TemperatureRankResponse;
import goorm.ddok.reputation.service.ReputationQueryService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReputationRankingScheduler {

    private final ReputationQueryService reputationQueryService;

    private final AtomicReference<TemperatureRankResponse> top1Cache = new AtomicReference<>();
    private final AtomicReference<Instant> lastUpdated = new AtomicReference<>();

    /** 서버 시작 시 바로 1회 실행 */
    @PostConstruct
    public void init() {
        updateTop1Ranking();
    }

    /**
     * 매 시간마다 TOP1 갱신
     * cron: 매시 정각 실행
     */
    @Scheduled(cron = "0 0 * * * *")
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
                    .updatedAt(now) // 🆕 세팅
                    .build();

            top1Cache.set(withUpdatedAt);
            lastUpdated.set(now);

            log.info("✅ TOP1 랭킹 갱신 완료: userId={}, temp={}, updatedAt={}",
                    top1.getUserId(), top1.getTemperature(), lastUpdated.get());
        } catch (Exception e) {
            log.error("❌ TOP1 랭킹 갱신 실패", e);
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
}
