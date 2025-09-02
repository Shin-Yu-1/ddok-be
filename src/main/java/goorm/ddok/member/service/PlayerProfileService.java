package goorm.ddok.member.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.*;
import goorm.ddok.member.dto.ProfileDto;
import goorm.ddok.member.dto.request.*;
import goorm.ddok.member.repository.*;
import goorm.ddok.reputation.domain.UserReputation;
import goorm.ddok.reputation.repository.UserReputationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerProfileService {

    private final UserRepository userRepository;
    private final UserPositionRepository userPositionRepository;
    private final UserTraitRepository userTraitRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserLocationRepository userLocationRepository;
    private final UserTechStackRepository userTechStackRepository;
    private final TechStackRepository techStackRepository;
    private final UserReputationRepository userReputationRepository;

    /* -------- 포지션 수정 -------- */
    public ProfileDto updatePositions(PositionsUpdateRequest req, CustomUserDetails me) {
        User user = requireMe(me);
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        String main = trimToNull(req.getMainPosition());
        if (main == null) throw new GlobalException(ErrorCode.PROFILE_MAIN_POSITION_REQUIRED);

        List<String> subsRaw = (req.getSubPositions() == null) ? List.of()
                : req.getSubPositions();

        // 정리(트림/빈값 제거)
        List<String> subs = subsRaw.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .toList();

        // 서브 2개 초과
        if (subs.size() > 2) throw new GlobalException(ErrorCode.PROFILE_SECONDARY_POSITION_TOO_MANY);

        // 메인과 중복 여부
        if (subs.stream().anyMatch(s -> s.equalsIgnoreCase(main))) {
            throw new GlobalException(ErrorCode.PROFILE_POSITION_DUPLICATED);
        }

        // 중복 제거(대소문자 구분 없이 동일 항목 제거)
        subs = subs.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(String::toLowerCase, s -> s, (a, b) -> a, LinkedHashMap::new),
                        m -> new ArrayList<>(m.values())
                ));

        // 기존 포지션 일괄 삭제 후 재구성
        var existing = new ArrayList<>(user.getPositions());
        user.getPositions().clear();
        userPositionRepository.deleteAll(existing);

        user.getPositions().add(UserPosition.primaryOf(user, main));
        short ord = 1;
        for (String s : subs) user.getPositions().add(UserPosition.secondaryOf(user, s, ord++));

        userRepository.save(user);
        return buildProfile(user, me);
    }

    /* -------- 트레이트 수정(전체 치환) -------- */
    public ProfileDto updateTraits(TraitsUpdateRequest req, CustomUserDetails me) {
        User user = requireMe(me);
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        var olds = new ArrayList<>(user.getTraits());
        user.getTraits().clear();
        userTraitRepository.deleteAll(olds);

        List<String> traits = (req.getTraits() == null) ? List.of()
                : req.getTraits().stream().map(this::trimToNull).filter(Objects::nonNull).distinct().toList();

        // (옵션) 유효성 강화가 필요하면 여기서 TRAIT_NAME_INVALID 던질 수 있음
        // if (traits.stream().anyMatch(t -> t.length() > 64)) throw new GlobalException(ErrorCode.TRAIT_NAME_INVALID);

        for (String t : traits) user.getTraits().add(UserTrait.builder().user(user).traitName(t).build());

        userRepository.save(user);
        return buildProfile(user, me);
    }

    /* -------- 활동 시간 수정 -------- */
    public ProfileDto updateActiveHours(ActiveHoursRequest req, CustomUserDetails me) {
        User user = requireMe(me);
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        int start = parseHour(req.getStart()); // "09" -> 9
        int end   = parseHour(req.getEnd());   // "18" -> 18

        // 비즈니스 검증: end >= start
        if (end < start) {
            throw new GlobalException(ErrorCode.ACTIVE_HOURS_RANGE_INVALID);
        }

        UserActivity activity = userActivityRepository.findByUserId(user.getId())
                .orElse(UserActivity.builder()
                        .user(user)
                        .activityStartTime(start)
                        .activityEndTime(end)
                        .build());

        activity = activity.toBuilder()
                .activityStartTime(start)
                .activityEndTime(end)
                .build();

        userActivityRepository.save(activity);
        return buildProfile(user, me);
    }


    /* -------- 활동 지역 수정 -------- */
    public ProfileDto updateLocation(LocationUpdateRequest req, CustomUserDetails me) {
        User user = requireMe(me);
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        var loc = req.getLocation();
        if (loc == null || loc.getLatitude() == null || loc.getLongitude() == null) {
            throw new GlobalException(ErrorCode.INVALID_LOCATION);
        }

        UserLocation ul = userLocationRepository.findByUserId(user.getId())
                .orElse(UserLocation.builder().user(user).build());

        ul = ul.toBuilder()
                .roadName(StringUtils.hasText(loc.getAddress()) ? loc.getAddress().trim() : null)
                .activityLatitude(BigDecimal.valueOf(loc.getLatitude()))
                .activityLongitude(BigDecimal.valueOf(loc.getLongitude()))
                .build();

        userLocationRepository.save(ul);
        return buildProfile(user, me);
    }

    /* -------- 자기 소개 (엔티티 미정 → TODO) -------- */
    public ProfileDto upsertContent(ContentUpdateRequest req, CustomUserDetails me) {
        User user = requireMe(me);
        // TODO: UserProfileExtra.content 저장(길이 검증 시 별도 에러코드 추가 가능)
        return buildProfile(user, me);
    }

    /* -------- 포트폴리오 (엔티티 미정 → TODO) -------- */
    public ProfileDto upsertPortfolio(PortfolioUpdateRequest req, CustomUserDetails me) {
        User user = requireMe(me);
        // TODO: UserPortfolio 저장(링크 형식 검증 시 별도 에러코드 추가 가능)
        return buildProfile(user, me);
    }

    /* -------- 공개/비공개 (엔티티 미정 → TODO) -------- */
    public ProfileDto updateVisibility(boolean isPublic, CustomUserDetails me) {
        User user = requireMe(me);
        // TODO: UserProfileExtra.isPublic 저장
        return buildProfile(user, me);
    }

    /* -------- 기술 스택 수정(전체 치환) -------- */
    public void updateTechStacks(TechStacksUpdateRequest req, CustomUserDetails me) {
        User user = requireMe(me);
        user = userRepository.findById(user.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        var olds = new ArrayList<>(user.getTechStacks());
        user.getTechStacks().clear();
        userTechStackRepository.deleteAll(olds);

        List<String> names = (req.getTechStacks() == null) ? List.of()
                : req.getTechStacks().stream().map(this::trimToNull).filter(Objects::nonNull).distinct().toList();

        // (옵션) 유효성 강화가 필요하면 여기서 TECH_STACK_NAME_INVALID 던질 수 있음
        // if (names.stream().anyMatch(n -> n.length() > 100)) throw new GlobalException(ErrorCode.TECH_STACK_NAME_INVALID);

        for (String name : names) {
            TechStack stack = techStackRepository.findByName(name).orElseGet(
                    () -> techStackRepository.save(TechStack.builder().name(name).build())
            );
            user.getTechStacks().add(UserTechStack.builder().user(user).techStack(stack).build());
        }
        userRepository.save(user);
    }
    /* -------- 공개/비공개 토글 -------- */
    public ProfileDto toggleVisibility(CustomUserDetails me) {
        User meUser = requireMe(me);

        // 최신 상태 로드 (영속 엔티티)
        User fresh = userRepository.findById(meUser.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        boolean current = fresh.isPublic();
        fresh.setPublic(!current);

        userRepository.save(fresh);

        return buildProfile(fresh, me);
    }

    /* -------- 공통: 프로필 빌드 -------- */
    @Transactional(readOnly = true)
    public ProfileDto buildProfile(User user, CustomUserDetails me) {
        User fresh = userRepository.findById(user.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        Long meId = (me != null && me.getUser() != null) ? me.getUser().getId() : null;

        // temperature (없으면 null)
        BigDecimal temp = userReputationRepository.findByUserId(fresh.getId())
                .map(UserReputation::getTemperature)
                .orElse(null);

        // main/sub positions
        String main = fresh.getPositions().stream()
                .filter(p -> p.getType() == UserPositionType.PRIMARY)
                .map(UserPosition::getPositionName)
                .findFirst().orElse(null);

        List<String> subs = fresh.getPositions().stream()
                .filter(p -> p.getType() == UserPositionType.SECONDARY)
                .sorted(Comparator.comparing(UserPosition::getOrd, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(UserPosition::getPositionName)
                .toList();

        // traits
        List<String> traits = fresh.getTraits().stream().map(UserTrait::getTraitName).toList();

        // active hours
        var actOpt = userActivityRepository.findByUserId(fresh.getId());
        ProfileDto.ActiveHours ah = actOpt
                .map(a -> new ProfileDto.ActiveHours(a.getActivityStartTime(), a.getActivityEndTime()))
                .orElse(null);

        // location
        var locOpt = userLocationRepository.findByUserId(fresh.getId());
        ProfileDto.LocationBlock loc = locOpt
                .map(l -> ProfileDto.LocationBlock.builder()
                        .latitude(toDouble(l.getActivityLatitude()))
                        .longitude(toDouble(l.getActivityLongitude()))
                        .address(l.getRoadName())
                        .build())
                .orElse(null);

        // tech stacks
        List<String> stacks = fresh.getTechStacks().stream()
                .map(uts -> uts.getTechStack().getName())
                .toList();

        return ProfileDto.builder()
                .userId(fresh.getId())
                .isMine(meId != null && Objects.equals(meId, fresh.getId()))
                .chatRoomId(null)
                .dmRequestPending(false)
                .isPublic(true)                 // TODO: 저장 구현 시 실제 값 매핑
                .profileImageUrl(fresh.getProfileImageUrl())
                .nickname(fresh.getNickname())
                .temperature(temp)              // null 허용
                .ageGroup(null)                 // TODO: birthDate 기반 계산 시 구현
                .mainPosition(main)
                .subPositions(subs)
                .mainBadge(null)                // 요구사항: 없으면 null
                .abandonBadge(null)             // 요구사항: 없으면 null
                .activeHours(ah)
                .traits(traits)
                .content(null)                  // TODO
                .portfolio(null)                // TODO
                .location(loc)
                .techStacks(stacks)
                .build();
    }

    /* -------- helpers -------- */
    private int parseHour(String hh) {
        if (!StringUtils.hasText(hh)) {
            throw new GlobalException(ErrorCode.ACTIVE_HOURS_FORMAT_INVALID);
        }
        try {
            int v = Integer.parseInt(hh);
            if (v < 0 || v > 24) throw new GlobalException(ErrorCode.ACTIVE_HOURS_FORMAT_INVALID);
            return v;
        } catch (NumberFormatException e) {
            throw new GlobalException(ErrorCode.ACTIVE_HOURS_FORMAT_INVALID);
        }
    }

    private User requireMe(CustomUserDetails me) {
        if (me == null || me.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);
        return me.getUser();
    }
    private String trimToNull(String s) {
        if (!StringUtils.hasText(s)) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private Double toDouble(BigDecimal v) { return (v == null) ? null : v.doubleValue(); }
}