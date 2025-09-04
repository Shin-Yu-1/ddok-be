package goorm.ddok.cafe.service;

import goorm.ddok.cafe.dto.response.CafeMapItemResponse;
import goorm.ddok.cafe.repository.CafeRepository;
import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CafeMapQueryService {

    private final CafeRepository cafeRepository;
    private final MapService mapService;

    @Transactional(readOnly = true)
    public List<CafeMapItemResponse> getCafesInBounds(
            BigDecimal swLat, BigDecimal swLng, BigDecimal neLat, BigDecimal neLng,
            BigDecimal centerLat, BigDecimal centerLng
    ) {
        mapService.validateBounds(swLat, swLng, neLat, neLng);

        var rows = cafeRepository.findAllInBounds(swLat, neLat, swLng, neLng);

        if (rows == null || rows.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        var items = rows.stream()
                .map(r -> CafeMapItemResponse.builder()
                        .category("cafe")
                        .cafeId(r.getId())
                        .title(r.getTitle())
                        .location(LocationDto.builder()
                                .address(mapService.composeRoadAddress(
                                        r.getRegion1depthName(),
                                        r.getRegion2depthName(),
                                        r.getRegion3depthName(),
                                        r.getRoadName(),
                                        r.getMainBuildingNo(),
                                        r.getSubBuildingNo()))
                                .region1depthName(r.getRegion1depthName())
                                .region2depthName(r.getRegion2depthName())
                                .region3depthName(r.getRegion3depthName())
                                .roadName(r.getRoadName())
                                .mainBuildingNo(r.getMainBuildingNo())
                                .subBuildingNo(r.getSubBuildingNo())
                                .zoneNo(r.getZoneNo())
                                .latitude(r.getLatitude())
                                .longitude(r.getLongitude())
                                .build())
                        .build())
                .toList();

        if (centerLat != null && centerLng != null && !items.isEmpty()) {
            final double cLat = centerLat.doubleValue();
            final double cLng = centerLng.doubleValue();

            items = items.stream()
                    .sorted(Comparator.comparingDouble(p ->
                            MapService.haversineKm(
                                    cLat, cLng,
                                    p.getLocation().getLatitude().doubleValue(),
                                    p.getLocation().getLongitude().doubleValue()
                            )))
                    .toList();
        }

        return items;
    }
}