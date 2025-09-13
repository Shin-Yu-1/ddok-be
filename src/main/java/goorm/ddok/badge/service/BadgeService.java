package goorm.ddok.badge.service;

import goorm.ddok.badge.domain.BadgeTier;
import goorm.ddok.badge.domain.BadgeTierRule;
import goorm.ddok.badge.domain.BadgeType;
import goorm.ddok.badge.domain.UserBadge;
import goorm.ddok.badge.repository.BadgeTierRuleRepository;
import goorm.ddok.badge.repository.UserBadgeRepository;
import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import goorm.ddok.member.domain.User;
import goorm.ddok.notification.event.BadgeAchievementEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BadgeService {
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeTierRuleRepository badgeTierRuleRepository;
    private final ApplicationEventPublisher publisher;

    /** 티어 규칙이 있는 배지 세트 */
    private static final Set<BadgeType> TIERED_BADGES =
            EnumSet.of(BadgeType.complete, BadgeType.leader_complete, BadgeType.login);

    /**
     * 배지 카운트 증가 (+ 이벤트 발행)
     *
     * - 특정 유저의 배지(totalCnt)를 +1 증가
     * - 배지 레코드가 없으면 생성 후 +1 (신규 획득으로 간주)
     * - 티어 배지(complete/leader_complete/login)에 대해:
     *   - 신규 생성 시: NEW_BADGE 이벤트 발행 (이때 newTier 포함)
     *   - 기존 보유 시: 티어 변경 감지되면 TIER_UP 이벤트 발행
     */
    @Transactional
    public UserBadge increaseBadge(User user, BadgeType badgeType) {
        var existing = userBadgeRepository.findByUserAndBadgeType(user, badgeType);
        UserBadge userBadge = existing.orElseGet(() -> UserBadge.create(user, badgeType));

        final boolean isNewRow = (userBadge.getId() == null);
        final int prevCnt = userBadge.getTotalCnt() == null ? 0 : userBadge.getTotalCnt();
        final BadgeTier prevTier = (TIERED_BADGES.contains(badgeType))
                ? resolveTier(badgeType, prevCnt)
                : null;

        // 카운트 증가 및 저장
        userBadge.increaseCount();
        UserBadge saved = userBadgeRepository.save(userBadge);

        // 이벤트 발행 (티어 배지에만)
        if (TIERED_BADGES.contains(badgeType)) {
            final int newCnt = saved.getTotalCnt();
            final BadgeTier newTier = resolveTier(badgeType, newCnt);

            if (isNewRow) {
                // 신규 생성 시에는 무조건 NEW_BADGE(complete의 bronze=0 규칙도 신규로 처리)
                publisher.publishEvent(new BadgeAchievementEvent(
                        user.getId(), badgeType, null, newTier, newCnt, BadgeAchievementEvent.Reason.NEW_BADGE
                ));
            } else if (newTier != prevTier) {
                // 기존 보유 상태에서 티어 상승
                publisher.publishEvent(new BadgeAchievementEvent(
                        user.getId(), badgeType, prevTier, newTier, newCnt, BadgeAchievementEvent.Reason.TIER_UP
                ));
            }
        }

        return saved;
    }

    /** 착한 배지 조회 */
    @Transactional(readOnly = true)
    public List<BadgeDto> getGoodBadges(User user) {
        List<UserBadge> badges = userBadgeRepository.findByUserAndBadgeTypeIn(
                user,
                List.of(BadgeType.complete, BadgeType.leader_complete, BadgeType.login)
        );
        return badges.stream().map(this::toGoodBadgeDto).toList();
    }

    /** 나쁜 배지 조회 (abandon) */
    @Transactional(readOnly = true)
    public AbandonBadgeDto getAbandonBadge(User user) {
        return userBadgeRepository.findByUserAndBadgeType(user, BadgeType.abandon)
                .map(badge -> AbandonBadgeDto.builder()
                        .IsGranted(badge.getTotalCnt() > 0)
                        .count(badge.getTotalCnt())
                        .build())
                .orElse(AbandonBadgeDto.builder().IsGranted(false).count(0).build());
    }

    /** 프로젝트/스터디 중도 하차 배지 부여 (abandon +1) */
    @Transactional
    public void grantAbandonBadge(User user) {
        increaseBadge(user, BadgeType.abandon);
    }

    /** 착한 배지 DTO 변환 (+ 현재 티어 계산) */
    private BadgeDto toGoodBadgeDto(UserBadge userBadge) {
        BadgeTier tier = resolveTier(userBadge.getBadgeType(), userBadge.getTotalCnt());
        return BadgeDto.builder()
                .type(userBadge.getBadgeType())
                .tier(tier)
                .build();
    }

    /** 누적 카운트 기준 티어 계산 (룰 없으면 bronze 기본) */
    private BadgeTier resolveTier(BadgeType type, int totalCnt) {
        return badgeTierRuleRepository
                .findTopByBadgeTypeAndRequiredCntLessThanEqualOrderByRequiredCntDesc(type, totalCnt)
                .map(BadgeTierRule::getTier)
                .orElse(BadgeTier.bronze);
    }

    /** 로그인 배지 (1일 1회) */
    @Transactional
    public void grantLoginBadge(User user) {
        boolean alreadyGrantedToday = userBadgeRepository.findByUserAndBadgeType(user, BadgeType.login)
                .map(badge -> badge.getUpdatedAt() != null &&
                        badge.getUpdatedAt().isAfter(Instant.now().truncatedTo(ChronoUnit.DAYS)))
                .orElse(false);
        if (!alreadyGrantedToday) {
            increaseBadge(user, BadgeType.login);
        }
    }

    /** 프로젝트/스터디 종료 배지 부여 */
    @Transactional
    public void grantCompleteBadge(User user, boolean isLeader) {
        if (isLeader) {
            increaseBadge(user, BadgeType.leader_complete);
        } else {
            increaseBadge(user, BadgeType.complete);
        }
    }

    /** 대표 착한 배지 조회 */
    @Transactional(readOnly = true)
    public BadgeDto getRepresentativeGoodBadge(User user) {
        List<UserBadge> badges = userBadgeRepository.findByUserAndBadgeTypeIn(
                user,
                List.of(BadgeType.complete, BadgeType.leader_complete, BadgeType.login)
        );
        return badges.stream()
                .max(Comparator.comparingInt(UserBadge::getTotalCnt)
                        .thenComparing(UserBadge::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(b -> BadgeDto.builder()
                        .type(b.getBadgeType())
                        .tier(resolveTier(b.getBadgeType(), b.getTotalCnt()))
                        .build())
                .orElse(null);
    }
}
