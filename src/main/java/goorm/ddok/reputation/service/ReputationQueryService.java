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
import goorm.ddok.reputation.dto.response.TemperatureRegionResponse;
import goorm.ddok.reputation.repository.UserReputationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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
    public List<TemperatureRegionResponse> getRegionTop1Rank(CustomUserDetails currentUser) {
        List<String> mainRegions = List.of("서울", "경기", "강원", "충청", "경상", "전라", "제주");

        return mainRegions.stream()
                .map(mainRegion -> {
                    List<String> rawRegions = getRawRegionsForMain(mainRegion);

                    UserReputation top1 = userReputationRepository.findFirstByUser_Location_Region1DepthNameInOrderByTemperatureDescUpdatedAtDesc(rawRegions)
                            .orElse(null);

                    if (top1 == null) return null;

                    User target = top1.getUser();

                    return TemperatureRegionResponse.builder()
                            .region(mainRegion)
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
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> getRawRegionsForMain(String mainRegion) {
        return switch (mainRegion) {
            case "서울" -> List.of("서울", "서울시", "서울특별시");
            case "경기도" -> List.of("경기", "인천", "경기도", "인천광역시");
            case "강원도" -> List.of("강원", "강원도", "강원특별자치도");
            case "충청도" -> List.of("충청", "충북", "충남", "세종", "충청북도", "충청남도", "대전광역시", "세종특별자치시", "세종시");
            case "경상도" -> List.of("경상", "경상북도", "경상남도", "대구광역시", "부산광역시", "울산광역시", "부산시", "울산시", "대구시", "경북", "경남", "대구", "부산", "울산");
            case "전라도" -> List.of("전라", "전라북도", "전라남도", "광주광역시", "전남", "전북", "광주");
            case "제주도" -> List.of("제주", "제주도", "제주특별자치도");
            default -> List.of();
        };
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
