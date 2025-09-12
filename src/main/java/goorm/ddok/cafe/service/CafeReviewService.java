package goorm.ddok.cafe.service;

import goorm.ddok.cafe.domain.*;
import goorm.ddok.cafe.dto.request.CafeReviewCreateRequest;
import goorm.ddok.cafe.dto.response.CafeReviewCreateResponse;
import goorm.ddok.cafe.repository.*;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CafeReviewService {

    private final CafeRepository cafeRepository;
    private final CafeReviewRepository cafeReviewRepository;
    private final CafeReviewTagRepository cafeReviewTagRepository;
    private final CafeReviewTagMapRepository cafeReviewTagMapRepository;
    private final UserRepository userRepository;

    @Transactional
    public CafeReviewCreateResponse createReview(Long cafeId, Long meUserId, CafeReviewCreateRequest req) {
        // 1) 카페/유저 검증
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CAFE_NOT_FOUND));

        User me = userRepository.findById(meUserId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        // 2) 평점 검증 (0.1 단위 소수1자리, 0.0 < rating ≤ 5.0)
        BigDecimal rating = normalizeAndValidateRating(req.getRating());

        // 4) 리뷰 생성
        CafeReview review = cafeReviewRepository.save(
                CafeReview.builder()
                        .cafe(cafe)
                        .user(me)
                        .rating(rating)
                        .status(CafeReviewStatus.ACTIVE)
                        .build()
        );
        // 요청 태그 정제
        List<String> names = Optional.ofNullable(req.getCafeReviewTag()).orElseGet(List::of).stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(20) // 안전 가드
                .toList();

// DB에서 실제 태그 조회
        List<CafeReviewTag> existingTags = cafeReviewTagRepository.findByNameIn(names);

// === 요청된 태그 수와 DB 조회된 태그 수가 다르면 => 잘못된 태그 포함 ===
        if (existingTags.size() != names.size()) {
            throw new GlobalException(ErrorCode.INVALID_REVIEW_TAG);
        }

// 정상적인 경우에만 저장 진행
        for (CafeReviewTag tag : existingTags) {
            cafeReviewTagMapRepository.save(
                    CafeReviewTagMap.builder()
                            .review(review)
                            .tag(tag)
                            .build()
            );
        }

        // 6) 응답 DTO
        return CafeReviewCreateResponse.builder()
                .userId(me.getId())
                .reviewId(review.getId())
                .title(cafe.getName())
                .nickname(me.getNickname())
                .profileImageUrl(me.getProfileImageUrl())
                .rating(review.getRating())
                .isMine(true)
                .cafeReviewTag(names)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    /** rating 검증/정규화: null/범위/스케일 체크 */
    private BigDecimal normalizeAndValidateRating(BigDecimal raw) {
        if (raw == null) throw new GlobalException(ErrorCode.INVALID_RATING);
        double r = raw.doubleValue();
        if (r <= 0.0 || r > 5.0) throw new GlobalException(ErrorCode.INVALID_RATING);
        // 0.5 단위 체크: r * 2 가 정수인지
        double times2 = r * 2.0;
        if (Math.abs(times2 - Math.round(times2)) > 1e-9) {
            throw new GlobalException(ErrorCode.INVALID_RATING);
        }
        return BigDecimal.valueOf(r).setScale(1, RoundingMode.HALF_UP);
    }
}