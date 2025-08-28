package goorm.ddok.player.service;

import goorm.ddok.global.dto.BadgeDto;
import goorm.ddok.member.domain.*;
import goorm.ddok.member.dto.response.ActiveHoursResponse;
import goorm.ddok.member.dto.response.LocationResponse;
import goorm.ddok.member.repository.UserPortfolioRepository;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.player.dto.response.ProfileResponse;
import goorm.ddok.player.dto.response.UserPortfolioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileQueryService {
    private final UserRepository userRepository;
    private final UserPortfolioRepository userPortfolioRepository;

    public ProfileResponse getProfile(Long targetUserId, Long loginUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        boolean isMine = targetUserId.equals(loginUserId);

        // 대표 배지 조회
        // TODO: 뱃지 구현 후 교체 (현재 null 처리)
        BadgeDto mainBadge = null;

        // 포트폴리오 조회
        List<UserPortfolioResponse> portfolios = userPortfolioRepository.findAllByUserId(targetUserId).stream()
                .map(p -> UserPortfolioResponse.builder()
                        .linkTitle(p.getLinkTitle())
                        .link(p.getLink())
                        .build())
                .collect(Collectors.toList());

        // 비공개 프로필 + 본인 아님 → 최소 정보만 노출
        if (!user.isPublic() && !isMine) {
            return buildPrivateProfile(user, isMine, mainBadge);
        }

        // 전체 공개 프로필
        return buildPublicProfile(user, isMine, mainBadge, portfolios);
    }

    /** 최소 정보 프로필 빌더 */
    private ProfileResponse buildPrivateProfile(User user, boolean isMine, BadgeDto mainBadge) {
        return ProfileResponse.builder()
                .userId(user.getId())
                .isMine(isMine)
                .isPublic(false)
                .profileImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .ageGroup(user.getAgeGroup())
                .mainBadge(mainBadge)
                .temperature(getSafeTemperature(user))
                .content(getSafeIntroduce(user))
                .build();
    }

    /** 전체 정보 프로필 빌더 */
    private ProfileResponse buildPublicProfile(User user,
                                               boolean isMine,
                                               BadgeDto mainBadge,
                                               List<UserPortfolioResponse> portfolios) {
        return ProfileResponse.builder()
                .userId(user.getId())
                .isMine(isMine)
                .chatRoomId(null) // TODO: DM 서비스 연동
                .dmRequestPending(false) // TODO: DM 상태 조회
                .isPublic(user.isPublic())
                .profileImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .temperature(getSafeTemperature(user))
                .ageGroup(user.getAgeGroup())
                .mainPosition(
                        user.getPositions().stream()
                                .filter(p -> p.getType() == UserPositionType.PRIMARY)
                                .findFirst()
                                .map(UserPosition::getPositionName)
                                .orElse(null)
                )
                .subPositions(
                        user.getPositions().stream()
                                .filter(p -> p.getType() == UserPositionType.SECONDARY)
                                .sorted(Comparator.comparing(UserPosition::getOrd))
                                .map(UserPosition::getPositionName)
                                .toList()
                )

                .mainBadge(mainBadge)
                .abandonBadge(null) // TODO: 탈주 배지 도메인 연결 필요
                .activeHours(user.getActivity() != null
                        ? ActiveHoursResponse.builder()
                        .start(user.getActivity().getActivityStartTime().toString())
                        .end(user.getActivity().getActivityEndTime().toString())
                        .build()
                        : null
                )
                 // 성향 traits (UserTrait 엔티티에 name 필드 있다고 가정)
                .traits(user.getTraits().stream()
                .map(UserTrait::getTraitName)
                .toList()
        )

                .content(getSafeIntroduce(user))
                .portfolio(portfolios)
                // 위치
                .location(user.getLocation() != null
                                ? new LocationResponse(
                                user.getLocation().getActivityLatitude() != null
                                        ? user.getLocation().getActivityLatitude().doubleValue()
                                        : null,
                                user.getLocation().getActivityLongitude() != null
                                        ? user.getLocation().getActivityLongitude().doubleValue()
                                        : null,
                                buildAddress(user.getLocation())
                        )
                                : null
                )
                .build();
    }

    /** 자기소개 반환 */
    private String getSafeIntroduce(User user) {
        if (user.getIntroduce() == null || user.getIntroduce().isBlank()) {
            return "자기 소개가 없습니다.";
        }
        return user.getIntroduce();
    }

    /** 안전한 온도 반환 */
    private double getSafeTemperature(User user) {
        if (user.getReputation() != null && user.getReputation().getTemperature() != null) {
            return user.getReputation().getTemperature();
        }
        return 36.5;
    }

    /** 주소 문자열 합치기 */
    private String buildAddress(UserLocation location) {
        StringBuilder sb = new StringBuilder();
        if (location.getRegion1DepthName() != null) sb.append(location.getRegion1DepthName()).append(" ");
        if (location.getRegion2DepthName() != null) sb.append(location.getRegion2DepthName()).append(" ");
        if (location.getRegion3DepthName() != null) sb.append(location.getRegion3DepthName()).append(" ");
        if (location.getRoadName() != null) sb.append(location.getRoadName()).append(" ");
        if (location.getZoneNo() != null) sb.append(location.getZoneNo());
        return sb.toString().trim();
    }
}