package goorm.ddok.player.service;

import goorm.ddok.badge.service.BadgeService;
import goorm.ddok.chat.service.ChatRoomService;
import goorm.ddok.chat.service.DmRequestCommandService;
import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserLocation;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

        // 페이지/사이즈 보정
        page = Math.max(page, 0);
        size = (size <= 0) ? 10 : size;

        // 닉네임 오름차순 (대소문자 무시)
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC, "nickname").ignoreCase());
        Pageable pageable = PageRequest.of(page, size, sort);

        // 기본 스펙: 항상 distinct 적용
        Specification<User> spec = (root, query, cb) -> {
            Objects.requireNonNull(query).distinct(true);
            return cb.conjunction();
        };

        if (!hasText(keyword)) {
            // 키워드 없으면 공개 프로필만
            spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isPublic")));
        } else {
            spec = spec.and(keywordSpec(keyword));
        }

        Page<User> rows = userRepository.findAll(spec, pageable);

        return rows.map(u -> toResponse(u, currentUserId));
    }

    private Specification<User> keywordSpec(String raw) {
        List<String> tokens = splitTokens(raw);

        return (root, query, cb) -> {
            query.distinct(true); // ★ count 쿼리에도 반영

            // 주소는 1:1이라 LEFT JOIN 사용해도 중복 없음
            Join<User, UserLocation> locJoin = root.join("location", JoinType.LEFT);

            List<Predicate> andPerToken = new ArrayList<>();

            for (String token : tokens) {
                String like = "%" + token.toLowerCase() + "%";

                List<Predicate> ors = new ArrayList<>();

                // 1) 닉네임 LIKE (공개 여부와 무관)
                ors.add(cb.like(cb.lower(root.get("nickname")), like));

                // 2) (isPublic AND EXISTS 포지션 LIKE)
                {
                    Subquery<Long> posSub = query.subquery(Long.class);
                    Root<UserPosition> p = posSub.from(UserPosition.class);
                    posSub.select(cb.literal(1L));
                    Predicate link = cb.equal(p.get("user").get("id"), root.get("id"));
                    Predicate posLike = cb.like(cb.lower(p.get("positionName")), like);
                    posSub.where(cb.and(link, posLike));
                    Predicate posExists = cb.exists(posSub);

                    ors.add(cb.and(cb.isTrue(root.get("isPublic")), posExists));
                }

                // 3) (isPublic AND 주소 필드 LIKE)
                ors.add(cb.and(cb.isTrue(root.get("isPublic")),
                        cb.like(cb.lower(cb.coalesce(locJoin.get("region1DepthName"), "")), like)));
                ors.add(cb.and(cb.isTrue(root.get("isPublic")),
                        cb.like(cb.lower(cb.coalesce(locJoin.get("region2DepthName"), "")), like)));
                ors.add(cb.and(cb.isTrue(root.get("isPublic")),
                        cb.like(cb.lower(cb.coalesce(locJoin.get("region3DepthName"), "")), like)));
                ors.add(cb.and(cb.isTrue(root.get("isPublic")),
                        cb.like(cb.lower(cb.coalesce(locJoin.get("roadName"), "")), like)));
                ors.add(cb.and(cb.isTrue(root.get("isPublic")),
                        cb.like(cb.lower(cb.coalesce(locJoin.get("mainBuildingNo"), "")), like)));
                ors.add(cb.and(cb.isTrue(root.get("isPublic")),
                        cb.like(cb.lower(cb.coalesce(locJoin.get("subBuildingNo"), "")), like)));

                // "서울 강남" 같은 합성 주소 매칭
                ors.add(cb.and(cb.isTrue(root.get("isPublic")),
                        cb.like(
                                cb.lower(
                                        cb.concat(
                                                cb.concat(cb.coalesce(locJoin.get("region1DepthName"), ""), " "),
                                                cb.coalesce(locJoin.get("region2DepthName"), "")
                                        )
                                ),
                                like
                        )));

                andPerToken.add(cb.or(ors.toArray(new Predicate[0])));
            }

            return cb.and(andPerToken.toArray(new Predicate[0]));
        };
    }

    private List<String> splitTokens(String raw) {
        return Arrays.stream(raw.split("[,\\s]+"))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();
    }

    private ProfileSearchResponse toResponse(User u, Long currentUserId) {

        boolean isMine = Objects.equals(u.getId(), currentUserId);
        boolean isPublic = u.isPublic();

        String address = null;
        String mainPosition = null;

        // isPublic이 true이거나 본인인 경우에만 주소와 포지션 정보 제공
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

        // 대표 배지 조회
        BadgeDto representative = badgeService.getRepresentativeGoodBadge(u);
        ProfileSearchResponse.MainBadge mainBadge = null;
        if (representative != null) {
            mainBadge = ProfileSearchResponse.MainBadge.builder()
                    .type(representative.getType().name())
                    .tier(representative.getTier().name())
                    .build();
        }

        // 나쁜 배지 조회
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
                .mainPosition(mainPosition) // isPublic이 false이면 null
                .address(address) // isPublic이 false이면 null
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
