package goorm.ddok.cafe.service;

import goorm.ddok.cafe.dto.response.CafeStatsResponse;
import goorm.ddok.cafe.dto.response.TagCountResponse;
import goorm.ddok.cafe.repository.CafeRepository;
import goorm.ddok.cafe.repository.CafeReviewRepository;
import goorm.ddok.cafe.repository.CafeReviewTagMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CafeStatsService {

    private final CafeRepository cafeRepository;
    private final CafeReviewRepository reviewRepository;
    private final CafeReviewTagMapRepository tagMapRepository;

    @Transactional(readOnly = true)
    public CafeStatsResponse getCafeStats(Long cafeId) {
        var cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("카페가 존재하지 않습니다. id=" + cafeId));

        long reviewCount = reviewRepository.countActiveByCafeId(cafeId);
        Double avg = reviewRepository.avgRatingActiveByCafeId(cafeId); // null-safe: 쿼리에서 coalesce(0)
        BigDecimal totalRating = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);

        var tagProjs = tagMapRepository.countTagsByCafeId(cafeId);
        List<TagCountResponse> tags = tagProjs.stream()
                .map(p -> new TagCountResponse(p.getTagName(), p.getTagCount()))
                .toList();

        return new CafeStatsResponse(
                cafe.getId(),
                cafe.getName(),
                reviewCount,
                tags,
                totalRating
        );
    }
}