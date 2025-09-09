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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeTierRuleRepository badgeTierRuleRepository;

    /**
     * 배지 카운트 증가
     *
     * 특정 유저의 배지(totalCnt)를 +1 증가시킨다.
     * 해당 배지가 존재하지 않으면 새로 생성한 후 카운트를 1로 설정한다.
     *
     * @param user      배지를 증가시킬 사용자
     * @param badgeType 증가시킬 배지 타입
     * @return 갱신된 UserBadge 엔티티
     */
    public UserBadge increaseBadge(User user, BadgeType badgeType) {
        UserBadge userBadge = userBadgeRepository.findByUserAndBadgeType(user, badgeType)
                .orElseGet(() -> UserBadge.create(user, badgeType));

        userBadge.increaseCount();

        return userBadgeRepository.save(userBadge);
    }

    /**
     * 착한 배지 조회
     *
     * complete, leader_complete, login 배지들을 조회하고,
     * 각 배지의 누적 횟수(totalCnt)에 따라 현재 티어(bronze/silver/gold)를 계산하여 반환한다.
     *
     * @param user 배지를 조회할 사용자
     * @return 착한 배지 리스트 (BadgeDto)
     */
    @Transactional(readOnly = true)
    public List<BadgeDto> getGoodBadges(User user) {
        List<UserBadge> badges = userBadgeRepository.findByUserAndBadgeTypeIn(
                user,
                List.of(BadgeType.complete, BadgeType.leader_complete, BadgeType.login)
        );

        return badges.stream()
                .map(this::toGoodBadgeDto)
                .toList();
    }

    /**
     * 나쁜 배지 조회
     *
     * abandon(탈주) 배지 조회.
     * 누적 횟수가 0보다 크면 획득한 것으로 간주한다.
     *
     * @param user 배지를 조회할 사용자
     * @return 나쁜 배지 정보 (AbandonBadgeDto)
     */
    @Transactional(readOnly = true)
    public AbandonBadgeDto getAbandonBadge(User user) {
        return userBadgeRepository.findByUserAndBadgeType(user, BadgeType.abandon)
                .map(badge -> AbandonBadgeDto.builder()
                        .IsGranted(badge.getTotalCnt() > 0)
                        .count(badge.getTotalCnt())
                        .build())
                .orElse(AbandonBadgeDto.builder()
                        .IsGranted(false)
                        .count(0)
                        .build());
    }

    /**
     * 착한 배지 변환
     *
     * UserBadge 엔티티를 BadgeDto로 변환하면서,
     * totalCnt 기준으로 티어를 계산하여 함께 반환한다.
     *
     * @param userBadge 변환할 유저 배지 엔티티
     * @return BadgeDto (배지 타입 + 티어)
     */
    private BadgeDto toGoodBadgeDto(UserBadge userBadge) {
        BadgeTier tier = calculateTier(userBadge.getBadgeType(), userBadge.getTotalCnt());
        return BadgeDto.builder()
                .type(userBadge.getBadgeType())
                .tier(tier)
                .build();
    }

    /**
     * 누적 카운트에 따른 티어 계산
     *
     * BadgeTierRule 테이블에서 totalCnt 이하의 가장 큰 requiredCnt를 찾아
     * 해당 룰의 티어(bronze/silver/gold)를 반환한다.
     * 조건에 맞는 룰이 없으면 bronze로 기본 설정된다.
     *
     * @param type     배지 타입
     * @param totalCnt 누적 카운트
     * @return 계산된 배지 티어
     */
    private BadgeTier calculateTier(BadgeType type, int totalCnt) {
        return badgeTierRuleRepository
                .findTopByBadgeTypeAndRequiredCntLessThanEqualOrderByRequiredCntDesc(type, totalCnt)
                .map(BadgeTierRule::getTier)
                .orElse(BadgeTier.bronze);
    }

    /**
     * 로그인 배지 부여 (1일 1회 제한)
     *
     * @param user 로그인한 사용자
     */
    @Transactional
    public void grantLoginBadge(User user) {
        // 오늘 이미 로그인 배지 반영했는지 확인
        boolean alreadyGrantedToday = userBadgeRepository.findByUserAndBadgeType(user, BadgeType.login)
                .map(badge -> badge.getUpdatedAt() != null &&
                        badge.getUpdatedAt().isAfter(Instant.now().truncatedTo(ChronoUnit.DAYS)))
                .orElse(false);

        if (!alreadyGrantedToday) {
            increaseBadge(user, BadgeType.login);
        }
    }

    /**
     * 프로젝트/스터디 종료 배지 부여
     *
     * 프로젝트 또는 스터디가 성공적으로 종료될 때 호출.
     * - 모든 참여자: complete 배지 증가
     * - 리더: leader_complete 배지 추가 증가
     *
     * @param user    배지를 부여할 사용자
     * @param isLeader 리더 여부
     */
    @Transactional
    public void grantCompleteBadge(User user, boolean isLeader) {
        increaseBadge(user, BadgeType.complete);
        if (isLeader) {
            increaseBadge(user, BadgeType.leader_complete);
        }
    }

    /**
     * 대표 착한 배지 조회
     *
     * complete, leader_complete, login 배지 중
     * 1순위 totalCnt (내림차순, max)
     * 2순위 updateAt (가장 최신)
     *
     * @param user 대표 배지를 조회할 사용자
     * @return 대표 배지 (없으면 null)
     */
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
                        .tier(calculateTier(b.getBadgeType(), b.getTotalCnt()))
                        .build())
                .orElse(null);
    }



}
