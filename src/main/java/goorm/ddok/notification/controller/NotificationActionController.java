package goorm.ddok.notification.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.notification.service.NotificationActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationActionController {

    private final NotificationActionService notificationActionService;

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponseDto<?>> accept(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        notificationActionService.accept(id, user);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청을 승인했습니다.", null));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponseDto<?>> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        notificationActionService.reject(id, user);
        return ResponseEntity.ok(ApiResponseDto.of(200, "요청을 거절했습니다.", null));
    }
}