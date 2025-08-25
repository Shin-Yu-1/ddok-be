package goorm.ddok.cafe.service;

import goorm.ddok.cafe.domain.Cafe;
import goorm.ddok.cafe.dto.response.CafeStatsResponse;
import goorm.ddok.cafe.dto.response.TagCountResponse;
import goorm.ddok.cafe.repository.CafeRepository;
import goorm.ddok.cafe.repository.CafeReviewRepository;
import goorm.ddok.cafe.repository.CafeReviewTagMapRepository;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CafeStatsService {

    private final CafeRepository cafeRepository;
    private final CafeReviewRepository reviewRepository;
    private final CafeReviewTagMapRepository tagMapRepository;

    @Transactional(readOnly = true)
    public CafeStatsResponse getCafeStats(Long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CAFE_NOT_FOUND));

        long reviewCount = reviewRepository.countActiveByCafeId(cafeId);

        Double avg = reviewRepository.avgRatingActiveByCafeId(cafeId);
        BigDecimal totalRating = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);

        List<CafeReviewTagMapRepository.TagCountProjection> tagProjections =
                tagMapRepository.countTagsByCafeId(cafeId);

        List<TagCountResponse> tags = tagProjections.stream()
                .map(p -> new TagCountResponse(p.getTagName(), p.getTagCount()))
                .sorted(
                        Comparator
                                .comparingLong(TagCountResponse::tagCount).reversed()
                                .thenComparing(TagCountResponse::tagName)
                )
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