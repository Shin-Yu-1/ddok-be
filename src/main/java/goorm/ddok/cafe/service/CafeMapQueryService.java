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

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CafeMapQueryService {

    private final CafeRepository cafeRepository;

    @Transactional(readOnly = true)
    public List<CafeMapItemResponse> getCafesInBounds(
            BigDecimal swLat, BigDecimal swLng, BigDecimal neLat, BigDecimal neLng,
            BigDecimal centerLat, BigDecimal centerLng
    ) {
        if (swLat.compareTo(neLat) > 0 || swLng.compareTo(neLng) > 0) {
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
                                c.composeFullAddress()
                        )
                ))
                .toList();
    }
}