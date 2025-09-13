package goorm.ddok.notification.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.notification.dto.response.NotificationResponse;
import goorm.ddok.notification.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    // 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponseDto<Map<String,Object>>> list(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String type
    ) {
        Long userId = user.getUser().getId();
        Page<NotificationResponse> p = notificationQueryService.list(userId, isRead, type, page, size);

        Map<String,Object> payload = Map.of(
                "content", p.getContent(),
                "page", p.getNumber(),
                "size", p.getSize(),
                "totalElements", p.getTotalElements(),
                "totalPages", p.getTotalPages(),
                "hasNext", p.hasNext()
        );

        return ResponseEntity.ok(ApiResponseDto.of(200, "알림 목록", payload));
    }

    // 안읽은 개수
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponseDto<Map<String, Long>>> unreadCount(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUser().getId();
        long count = notificationQueryService.unreadCount(userId);
        return ResponseEntity.ok(ApiResponseDto.of(200, "미확인 알림 수", Map.of("count", count)));
    }

    // 단건 읽음 처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponseDto<Void>> markRead(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long id
    ) {
        Long userId = user.getUser().getId();
        notificationQueryService.markRead(userId, id);
        return ResponseEntity.ok(ApiResponseDto.of(200, "읽음 처리 완료", null));
    }
}
