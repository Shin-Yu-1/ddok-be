package goorm.ddok.cafe.service;

import goorm.ddok.cafe.domain.Cafe;
import goorm.ddok.cafe.domain.CafeReview;
import goorm.ddok.cafe.dto.response.*;
import goorm.ddok.cafe.repository.CafeRepository;
import goorm.ddok.cafe.repository.CafeReviewRepository;
import goorm.ddok.cafe.repository.CafeReviewTagMapRepository;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeReviewQueryService {

    private final CafeRepository cafeRepository;
    private final CafeReviewRepository reviewRepository;
    private final CafeReviewTagMapRepository tagMapRepository;

    @Transactional(readOnly = true)
    public CafeReviewListResponse getCafeReviews(Long cafeId, int page, int size, String sort) {
        // 1) 카페 존재 확인
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CAFE_NOT_FOUND));

        // 2) 정렬 파라미터 → Pageable
        Sort sortSpec = switch (Optional.ofNullable(sort).orElse("recent")) {
            case "rating_desc" -> Sort.by(Sort.Order.desc("rating").ignoreCase());
            case "rating_asc"  -> Sort.by(Sort.Order.asc("rating").ignoreCase());
            default            -> Sort.by(Sort.Order.desc("createdAt")); // recent
        };
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        // 3) 리뷰 페이지 조회 (user 조인 포함)
        Page<CafeReview> pageResult = reviewRepository.findPageActiveByCafeId(cafeId, pageable);

        // 4) 현재 페이지의 리뷰 IDs
        List<Long> reviewIds = pageResult.getContent().stream()
                .map(CafeReview::getId)
                .toList();

        // 5) 태그 이름 묶기 (reviewId → [tagName...])
        Map<Long, List<String>> tagMap = tagMapRepository.findTagNamesByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.groupingBy(
                        CafeReviewTagMapRepository.ReviewTagProjection::getReviewId,
                        Collectors.mapping(CafeReviewTagMapRepository.ReviewTagProjection::getTagName, Collectors.toList())
                ));

        // 6) 아이템 변환
        List<CafeReviewItemResponse> items = pageResult.getContent().stream()
                .map(r -> new CafeReviewItemResponse(
                        r.getUser().getId(),
                        r.getUser().getNickname(),
                        r.getUser().getProfileImageUrl(),
                        r.getRating(),
                        tagMap.getOrDefault(r.getId(), Collections.emptyList()),
                        r.getCreatedAt(),
                        r.getUpdatedAt()
                ))
                .toList();

        // 7) 페이지메타 + 상위 응답
        PageMetaResponse pagination = new PageMetaResponse(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements()
        );

        return new CafeReviewListResponse(
                cafe.getId(),
                cafe.getName(),
                pagination,
                items
        );
    }
}