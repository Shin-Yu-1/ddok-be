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
    public CafeReviewListResponse getCafeReviews(Long cafeId, int page, int size) {

        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CAFE_NOT_FOUND));


        Sort sortSpec = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        );
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        Page<CafeReview> pageResult = reviewRepository.findPageActiveByCafeId(cafeId, pageable);

        List<Long> reviewIds = pageResult.getContent().stream()
                .map(CafeReview::getId)
                .toList();

        Map<Long, List<String>> tagMap = tagMapRepository.findTagNamesByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.groupingBy(
                        CafeReviewTagMapRepository.ReviewTagProjection::getReviewId,
                        Collectors.mapping(CafeReviewTagMapRepository.ReviewTagProjection::getTagName, Collectors.toList())
                ));

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