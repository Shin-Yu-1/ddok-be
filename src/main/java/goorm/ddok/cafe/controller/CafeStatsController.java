package goorm.ddok.cafe.controller;

import goorm.ddok.cafe.dto.response.CafeStatsResponse;
import goorm.ddok.cafe.service.CafeStatsService;
import goorm.ddok.global.response.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map/cafes")
public class CafeStatsController {

    private final CafeStatsService cafeStatsService;

    @GetMapping("/{cafeId}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long cafeId) {
        CafeStatsResponse data = cafeStatsService.getCafeStats(cafeId);

        return ResponseEntity.ok(ApiResponseDto.of(200,"카페 후기 통계 조회에 성공하였습니다.",data));
    }
}