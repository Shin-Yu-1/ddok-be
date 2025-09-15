package goorm.ddok.player.service;

import goorm.ddok.badge.service.BadgeService;
import goorm.ddok.chat.service.ChatRoomService;
import goorm.ddok.chat.service.DmRequestCommandService;
import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.player.dto.response.ProfileSearchResponse;
import goorm.ddok.reputation.domain.UserReputation;
import goorm.ddok.reputation.repository.UserReputationRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.util.StringUtils.hasText;


import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProfileSearchService {

    private final UserRepository userRepository;
    private final UserReputationRepository userReputationRepository;
    private final BadgeService badgeService;
    private final ChatRoomService chatRoomService;
    private final DmRequestCommandService dmRequestService;

    @Transactional(readOnly = true)
    public Page<ProfileSearchResponse> searchPlayers(String keyword, int page, int size, Long currentUserId) {
        page = Math.max(page, 0);
        size = (size <= 0) ? 10 : size;

        Pageable pageable = PageRequest.of(page, size);

        String searchKeyword = hasText(keyword) ? keyword.trim() : null;

        Page<User> rows = userRepository.searchPlayersWithKeyword(searchKeyword, pageable);
        return rows.map(u -> toResponse(u, currentUserId));
    }

    // 나머지 메서드들은 그대로 유지
    private ProfileSearchResponse toResponse(User u, Long currentUserId) {
        // 기존 코드 그대로
        boolean isMine = Objects.equals(u.getId(), currentUserId);
        boolean isPublic = u.isPublic();

        String address = null;
        String mainPosition = null;

        if (isPublic || isMine) {
            address = shortAddress(
                    u.getLocation() == null ? null : u.getLocation().getRegion1DepthName(),
                    u.getLocation() == null ? null : u.getLocation().getRegion2DepthName()
            );
            mainPosition = pickMainPosition(u);
        }

        BigDecimal temp = userReputationRepository.findByUserId(u.getId())
                .map(UserReputation::getTemperature)
                .orElse(null);

        BadgeDto representative = badgeService.getRepresentativeGoodBadge(u);
        ProfileSearchResponse.MainBadge mainBadge = null;
        if (representative != null) {
            mainBadge = ProfileSearchResponse.MainBadge.builder()
                    .type(representative.getType().name())
                    .tier(representative.getTier().name())
                    .build();
        }

        AbandonBadgeDto abandon = badgeService.getAbandonBadge(u);
        ProfileSearchResponse.AbandonBadge abandonBadge =
                ProfileSearchResponse.AbandonBadge.builder()
                        .IsGranted(abandon.isIsGranted())
                        .count(abandon.getCount())
                        .build();

        Long chatRoomId = null;
        boolean dmPending = false;
        if (!isMine && currentUserId != null) {
            chatRoomId = chatRoomService.findPrivateRoomId(currentUserId, u.getId()).orElse(null);
            dmPending = (chatRoomId != null)
                    || dmRequestService.isDmPendingOrAcceptedOrChatExists(currentUserId, u.getId());
        }

        return ProfileSearchResponse.builder()
                .userId(u.getId())
                .category("players")
                .nickname(u.getNickname())
                .profileImageUrl(u.getProfileImageUrl())
                .mainBadge(mainBadge)
                .abandonBadge(abandonBadge)
                .mainPosition(mainPosition)
                .address(address)
                .temperature(temp)
                .IsMine(currentUserId != null && currentUserId.equals(u.getId()))
                .chatRoomId(chatRoomId)
                .dmRequestPending(dmPending)
                .build();
    }

    private String pickMainPosition(User u) {
        if (u.getPositions() == null || u.getPositions().isEmpty()) return null;

        Optional<UserPosition> primary = u.getPositions().stream()
                .filter(p -> p.getType() == UserPositionType.PRIMARY)
                .findFirst();
        if (primary.isPresent()) return primary.get().getPositionName();

        Optional<UserPosition> sec1 = u.getPositions().stream()
                .filter(p -> p.getType() == UserPositionType.SECONDARY && Short.valueOf((short)1).equals(p.getOrd()))
                .findFirst();
        if (sec1.isPresent()) return sec1.get().getPositionName();

        return u.getPositions().get(0).getPositionName();
    }

    private String shortAddress(String region1, String region2) {
        String r1 = region1 == null ? "" : region1.trim();
        String r2 = region2 == null ? "" : region2.trim();
        if (r1.isEmpty() && r2.isEmpty()) return "-";
        return (r1 + " " + r2).trim();
    }
}
