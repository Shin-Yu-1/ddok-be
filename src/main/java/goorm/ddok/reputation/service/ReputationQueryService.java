package goorm.ddok.reputation.service;

import goorm.ddok.badge.service.BadgeService;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.reputation.domain.UserReputation;
import goorm.ddok.reputation.dto.response.TemperatureMeResponse;
import goorm.ddok.reputation.dto.response.TemperatureRankResponse;
import goorm.ddok.reputation.repository.UserReputationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReputationQueryService {

    private final UserReputationRepository userReputationRepository;
    private final BadgeService badgeService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TemperatureRankResponse> getTop10TemperatureRank(
            CustomUserDetails currentUser
    ) {
        List<UserReputation> top10 = userReputationRepository.findTop10ByOrderByTemperatureDescUpdatedAtDesc();

        return IntStream.range(0, top10.size())
                .mapToObj(i -> {
                    User target = top10.get(i).getUser();
                    return TemperatureRankResponse.builder()
                            .rank(i + 1)
                            .userId(target.getId())
                            .nickname(target.getNickname())
                            .temperature(top10.get(i).getTemperature())
                            .mainPosition(extractMainPosition(target))
                            .profileImageUrl(target.getProfileImageUrl())
                            .chatRoomId(null)          // 캐싱 단계에서는 DM 정보 없음
                            .dmRequestPending(false)   // 캐싱 단계에서는 DM 정보 없음
                            .IsMine(currentUser != null && target.getId().equals(currentUser.getId()))
                            .mainBadge(badgeService.getRepresentativeGoodBadge(target))
                            .abandonBadge(badgeService.getAbandonBadge(target))
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public TemperatureRankResponse getTop1TemperatureRank(CustomUserDetails currentUser) {

        UserReputation top1 = userReputationRepository
                .findTop1ByOrderByTemperatureDescUpdatedAtDesc()
                .orElseThrow(() -> new GlobalException(ErrorCode.REPUTATION_NOT_FOUND));

        User target = top1.getUser();
        if (target == null) {
            throw new GlobalException(ErrorCode.USER_NOT_FOUND);
        }

        return TemperatureRankResponse.builder()
                .rank(1)
                .userId(target.getId())
                .nickname(target.getNickname())
                .temperature(top1.getTemperature())
                .mainPosition(extractMainPosition(target))
                .profileImageUrl(target.getProfileImageUrl())
                .chatRoomId(null)          // 캐싱 단계에서는 DM 정보 없음
                .dmRequestPending(false)   // 캐싱 단계에서는 DM 정보 없음
                .IsMine(currentUser != null && target.getId().equals(currentUser.getId()))
                .mainBadge(badgeService.getRepresentativeGoodBadge(target))
                .abandonBadge(badgeService.getAbandonBadge(target))
                .updatedAt(top1.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public TemperatureMeResponse getMyTemperature(CustomUserDetails currentUser) {
        if (currentUser == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

//        User user = currentUser.getUser();
//        if(user == null) {
//            throw new GlobalException(ErrorCode.USER_NOT_FOUND);
//        }
        User user = userRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        UserReputation reputation = userReputationRepository.findByUser(user)
                .orElseThrow(() -> new GlobalException(ErrorCode.REPUTATION_NOT_FOUND));

        return TemperatureMeResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .temperature(reputation.getTemperature())
                .mainPosition(extractMainPosition(user))
                .profileImageUrl(user.getProfileImageUrl())
                .mainBadge(badgeService.getRepresentativeGoodBadge(user))
                .abandonBadgeDto(badgeService.getAbandonBadge(user))
                .build();

    }

    /**
     * 사용자 메인 포지션 추출
     * PRIMARY 포지션을 찾아서 이름 반환
     * 없으면 null
     */
    private String extractMainPosition(User user) {
        return user.getPositions().stream()
                .filter(pos -> pos.getType() == UserPositionType.PRIMARY)
                .map(UserPosition::getPositionName)
                .findFirst()
                .orElse(null);
    }
}
