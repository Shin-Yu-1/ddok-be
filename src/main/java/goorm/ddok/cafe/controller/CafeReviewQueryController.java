package goorm.ddok.cafe.controller;

import goorm.ddok.cafe.dto.response.CafeReviewListResponse;
import goorm.ddok.cafe.service.CafeReviewQueryService;
import goorm.ddok.global.response.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/cafes")
public class CafeReviewQueryController {

    private final CafeReviewQueryService service;

    @GetMapping("/{cafeId}/reviews")
    public ResponseEntity<?> getReviews(
            @PathVariable Long cafeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        CafeReviewListResponse data = service.getCafeReviews(cafeId, page, size);

        return ResponseEntity.ok(ApiResponseDto.of(200, "카페 후기 리스트 조회에 성공하였습니다.", data));
    }
}