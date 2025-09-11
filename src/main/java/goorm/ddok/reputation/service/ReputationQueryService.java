package goorm.ddok.reputation.service;

import goorm.ddok.badge.service.BadgeService;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import goorm.ddok.reputation.domain.UserReputation;
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

    @Transactional(readOnly = true)
    public List<TemperatureRankResponse> getTop10TemperatureRank(
            CustomUserDetails currentUser
    ) {
        if (currentUser == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        List<UserReputation> top10 = userReputationRepository.findTop10ByOrderByTemperatureDescUpdatedAtDesc();

        return IntStream.range(0, top10.size())
                .mapToObj(i -> {
                    User target = top10.get(i).getUser();
                    if (target == null) {
                        throw new GlobalException(ErrorCode.USER_NOT_FOUND);
                    }

                    // TODO: DM 채팅방 확인 로직 필요
                    Long chatRoomId = null;
                    boolean dmRequestPending = false;

                    return TemperatureRankResponse.builder()
                            .rank(i + 1)
                            .userId(target.getId())
                            .nickname(target.getNickname())
                            .temperature(top10.get(i).getTemperature())
                            .mainPosition(extractMainPosition(target))
                            .profileImageUrl(target.getProfileImageUrl())
                            .chatRoomId(chatRoomId)
                            .dmRequestPending(dmRequestPending)
                            .IsMine(target.getId().equals(currentUser.getId()))
                            .mainBadge(badgeService.getRepresentativeGoodBadge(target))
                            .abandonBadge(badgeService.getAbandonBadge(target))
                            .build();
                })
                .toList();
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
