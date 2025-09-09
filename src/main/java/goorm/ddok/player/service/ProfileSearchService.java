package goorm.ddok.player.service;

import goorm.ddok.badge.service.BadgeService;
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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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

    @Transactional(readOnly = true)
    public Page<ProfileSearchResponse> searchPlayers(String keyword, int page, int size, Long currentUserId) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.asc("nickname"), Sort.Order.asc("id"))
        );
        Specification<User> spec = Specification.where(null);

        if (hasText(keyword)) {
            spec = spec.and(keywordSpec(keyword));
        }

        Page<User> rows = userRepository.findAll(spec, pageable);

        return rows.map(u -> toResponse(u, currentUserId));
    }

    private Specification<User> keywordSpec(String raw) {
        List<String> tokens = splitTokens(raw);

        return (root, q, cb) -> {
            if (Objects.requireNonNull(q).getResultType() != Long.class && q.getResultType() != long.class) {
                q.distinct(true);
            }

            Join<User, UserPosition> posJoin = root.join("positions", JoinType.LEFT);
            Join<User, UserLocation> locJoin = root.join("location", JoinType.LEFT);

            List<Predicate> andPerToken = new ArrayList<>();
            for (String token : tokens) {
                String like = "%" + token.toLowerCase() + "%";

                List<Predicate> orPredicates = new ArrayList<>();

                orPredicates.add(cb.like(cb.lower(root.get("nickname")), like));

                Predicate isPublicCondition = cb.isTrue(root.get("isPublic"));

                orPredicates.add(
                        cb.and(
                                isPublicCondition,
                                cb.like(cb.lower(posJoin.get("positionName")), like)
                        )
                );

                orPredicates.add(
                        cb.and(
                                isPublicCondition,
                                cb.like(cb.lower(cb.coalesce(locJoin.get("region1DepthName"), "")), like)
                        )
                );

                orPredicates.add(
                        cb.and(
                                isPublicCondition,
                                cb.like(cb.lower(cb.coalesce(locJoin.get("region2DepthName"), "")), like)
                        )
                );

                orPredicates.add(
                        cb.and(
                                isPublicCondition,
                                cb.like(cb.lower(cb.coalesce(locJoin.get("region3DepthName"), "")), like)
                        )
                );

                orPredicates.add(
                        cb.and(
                                isPublicCondition,
                                cb.like(cb.lower(cb.coalesce(locJoin.get("roadName"), "")), like)
                        )
                );

                orPredicates.add(
                        cb.and(
                                isPublicCondition,
                                cb.like(cb.lower(cb.coalesce(locJoin.get("mainBuildingNo"), "")), like)
                        )
                );

                orPredicates.add(
                        cb.and(
                                isPublicCondition,
                                cb.like(cb.lower(cb.coalesce(locJoin.get("subBuildingNo"), "")), like)
                        )
                );

                orPredicates.add(
                        cb.and(
                                isPublicCondition,
                                cb.like(
                                        cb.lower(
                                                cb.concat(
                                                        cb.concat(cb.coalesce(locJoin.get("region1DepthName"), ""), " "),
                                                        cb.coalesce(locJoin.get("region2DepthName"), "")
                                                )
                                        ),
                                        like
                                )
                        )
                );

                Predicate orForOneToken = cb.or(orPredicates.toArray(new Predicate[0]));
                andPerToken.add(orForOneToken);
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
                .chatRoomId(null) // TODO: 채팅 도메인 연동
                .dmRequestPending(false) // TODO: DM 도메인 연동
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
