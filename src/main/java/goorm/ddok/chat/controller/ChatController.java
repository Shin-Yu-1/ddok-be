package goorm.ddok.chat.controller;


import goorm.ddok.chat.dto.response.ChatListResponseDto;
import goorm.ddok.chat.service.ChatService;
import goorm.ddok.global.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/private")
    @Operation(summary = "개인 채팅 목록 조회", description = "사용자의 1:1 개인 채팅 목록을 조회합니다.")
    public ResponseEntity<ApiResponseDto<ChatListResponseDto>> getPrivateChats(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size);
        Long userId = Long.valueOf(authentication.getName());

        ChatListResponseDto response = chatService.getPrivateChats(userId, pageable);

        return ResponseEntity.ok(ApiResponseDto.of(
                200,
                "개인 채팅 목록 조회 성공",
                response
        ));
    }

    @GetMapping("/team")
    @Operation(summary = "팀 채팅 목록 조회", description = "사용자의 그룹 채팅 목록을 조회합니다.")
    public ResponseEntity<ApiResponseDto<ChatListResponseDto>> getTeamChats(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size);
        Long userId = Long.valueOf(authentication.getName());

        ChatListResponseDto response = chatService.getTeamChats(userId, pageable);

        return ResponseEntity.ok(ApiResponseDto.of(
                200,
                "팀 채팅 목록 조회 성공",
                response
        ));
    }
}
