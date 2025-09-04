package goorm.ddok.member.service;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserLocation;
import goorm.ddok.member.dto.request.LocationUpdateRequest;
import goorm.ddok.member.dto.response.LocationResponse;
import goorm.ddok.member.repository.UserLocationRepository;
import goorm.ddok.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLocationService {

    private final UserRepository userRepository;
    private final UserLocationRepository userLocationRepository;

    public LocationResponse updateLocation(LocationUpdateRequest req, CustomUserDetails me) {
        if (me == null || me.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);
        if (req == null || req.getLocation() == null) throw new GlobalException(ErrorCode.INVALID_LOCATION);

        User user = userRepository.findById(me.getUser().getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        LocationDto in = req.getLocation();

        // 위경도 최소 검증
        if (in.getLatitude() == null || in.getLongitude() == null) {
            throw new GlobalException(ErrorCode.INVALID_LOCATION);
        }

        UserLocation loc = userLocationRepository.findByUserId(user.getId())
                .orElse(UserLocation.builder().user(user).build());

        // 저장(트림 + 스케일 고정)
        loc = loc.toBuilder()
                .region1DepthName(trimToNull(in.getRegion1depthName()))
                .region2DepthName(trimToNull(in.getRegion2depthName()))
                .region3DepthName(trimToNull(in.getRegion3depthName()))
                .roadName(trimToNull(in.getRoadName()))
                .mainBuildingNo(trimToNull(in.getMainBuildingNo()))
                .subBuildingNo(trimToNull(in.getSubBuildingNo()))
                .zoneNo(trimToNull(in.getZoneNo()))
                .activityLatitude(scale6(in.getLatitude()))
                .activityLongitude(scale6(in.getLongitude()))
                .build();

        UserLocation saved = userLocationRepository.save(loc);

        // 응답 address는 합성해서 내려줌
        String address = composeFullAddress(
                saved.getRegion1DepthName(),
                saved.getRegion2DepthName(),
                saved.getRegion3DepthName(),
                saved.getRoadName(),
                saved.getMainBuildingNo(),
                saved.getSubBuildingNo()
        );

        return LocationResponse.builder()
                .address(address)
                .region1depthName(saved.getRegion1DepthName())
                .region2depthName(saved.getRegion2DepthName())
                .region3depthName(saved.getRegion3DepthName())
                .roadName(saved.getRoadName())
                .mainBuildingNo(saved.getMainBuildingNo())
                .subBuildingNo(saved.getSubBuildingNo())
                .zoneNo(saved.getZoneNo())
                .latitude(saved.getActivityLatitude())
                .longitude(saved.getActivityLongitude())
                .build();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static BigDecimal scale6(BigDecimal v) {
        return (v == null) ? null : v.setScale(6, RoundingMode.HALF_UP);
    }

    /** "서울특별시 강남구 역삼동 테헤란로 123-45" 형태로 합성 */
    private static String composeFullAddress(String r1, String r2, String r3, String road, String main, String sub) {
        StringBuilder sb = new StringBuilder();
        if (notBlank(r1)) sb.append(r1).append(" ");
        if (notBlank(r2)) sb.append(r2).append(" ");
        if (notBlank(r3)) sb.append(r3).append(" ");
        if (notBlank(road)) sb.append(road).append(" ");
        if (notBlank(main)) {
            sb.append(main);
            if (notBlank(sub)) sb.append("-").append(sub);
        }
        String s = sb.toString().trim().replaceAll("\\s+"," ");
        return s.isBlank() ? null : s;
    }
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
}