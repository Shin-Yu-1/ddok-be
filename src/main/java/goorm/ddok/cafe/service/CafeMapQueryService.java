package goorm.ddok.cafe.service;

import goorm.ddok.cafe.domain.Cafe;
import goorm.ddok.cafe.dto.response.CafeLocationResponse;
import goorm.ddok.cafe.dto.response.CafeMapItemResponse;
import goorm.ddok.cafe.repository.CafeRepository;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CafeMapQueryService {

    private final CafeRepository cafeRepository;

    @Transactional(readOnly = true)
    public List<CafeMapItemResponse> getCafesInBounds(
            double swLat, double swLng, double neLat, double neLng,
            Double centerLat, Double centerLng
    ) {

        if (swLat > neLat || swLng > neLng) {
            throw new GlobalException(ErrorCode.INVALID_BOUNDING_BOX);
        }

        List<Cafe> cafes = cafeRepository.findActiveWithinBounds(swLat, swLng, neLat, neLng);

        return cafes.stream()
                .map(c -> new CafeMapItemResponse(
                        "cafe",
                        c.getId(),
                        c.getName(),
                        new CafeLocationResponse(
                                c.getActivityLatitude(),
                                c.getActivityLongitude(),
                                buildAddress(c)
                        )
                ))
                .toList();
    }

    private String buildAddress(Cafe c) {
        // 도로명 주소가 있으면 그걸 우선 사용, 없으면 행정동 조합
        if (c.getRoadName() != null && !c.getRoadName().isBlank()) {
            if (c.getZoneNo() != null && !c.getZoneNo().isBlank()) {
                return c.getRoadName() + " (" + c.getZoneNo() + ")";
            }
            return c.getRoadName();
        }
        String r1 = nullToEmpty(c.getRegion1depthName());
        String r2 = nullToEmpty(c.getRegion2depthName());
        String r3 = nullToEmpty(c.getRegion3depthName());
        String joined = String.join(" ", new String[]{r1, r2, r3}).trim().replaceAll(" +", " ");
        return joined.isEmpty() ? "-" : joined;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}