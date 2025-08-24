package goorm.ddok.chat.controller;


import goorm.ddok.chat.dto.request.ChatMaessageRequest;
import goorm.ddok.chat.dto.response.ChatListResponseResponse;
import goorm.ddok.chat.dto.response.ChatMembersResponse;
import goorm.ddok.chat.dto.response.ChatMessageResponse;
import goorm.ddok.chat.service.ChatService;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.member.dto.request.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/private")
    @Operation(summary = "개인 채팅 목록 조회", description = "사용자의 1:1 개인 채팅 목록을 조회합니다.")
    public ResponseEntity<ApiResponseDto<ChatListResponseResponse>> getPrivateChats(
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        if (search != null && search.trim().isEmpty()) {
            throw new GlobalException(ErrorCode.INVALID_SEARCH_KEYWORD);
        }

        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size);
        String email = authentication.getName();

        ChatListResponseResponse response;
        if (search == null) {
            // 검색 파라미터 아예 없음 → 기존 목록 조회
            response = chatService.getPrivateChats(email, pageable);
        } else {
            // 검색 파라미터 존재 → 검색 로직 실행
            response = chatService.searchPrivateChats(email, search.trim(), pageable);
        }

        return ResponseEntity.ok(ApiResponseDto.of(
                200,
                "개인 채팅 목록 조회 성공",
                response
        ));
    }

    @GetMapping("/team")
    @Operation(summary = "팀 채팅 목록 조회", description = "사용자의 그룹 채팅 목록을 조회합니다.")
    public ResponseEntity<ApiResponseDto<ChatListResponseResponse>> getTeamChats(
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        if (search != null && search.trim().isEmpty()) {
            throw new GlobalException(ErrorCode.INVALID_SEARCH_KEYWORD);
        }

        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size);
        String email = authentication.getName();

        ChatListResponseResponse response;
        if (search == null) {
            response = chatService.getTeamChats(email, pageable);
        } else {
            response = chatService.searchTeamChats(email, search.trim(), pageable);
        }

        return ResponseEntity.ok(ApiResponseDto.of(
                200,
                "팀 채팅 목록 조회 성공",
                response
        ));
    }

    @GetMapping("/{roomId}/members")
    @Operation(
            summary = "채팅방 전체 인원 조회",
            description = "roomId로 지정된 채팅방의 모든 멤버 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponseDto<ChatMembersResponse>> getMembers(
            @Parameter(
                    description = "채팅방 ID",
                    example = "123",
                    required = true,
                    in = ParameterIn.PATH  // 이 부분이 중요!
            )
            @PathVariable Long roomId,  // "roomId" 생략 가능 (이름이 같을 때)
            Authentication authentication
    ) {
        if (roomId == null || roomId <= 0) {
            throw new GlobalException(ErrorCode.INVALID_ROOM_ID);
        }

        String email = authentication.getName();

        ChatMembersResponse response = chatService.getRoomMembers(roomId, email);

        return ResponseEntity.ok(ApiResponseDto.of(
                200,
                "채팅방 인원 조회 성공",
                response
        ));
    }


    @PostMapping("/{roomId}/messages")
    @Operation(
            summary = "채팅 메세지 전송",
            description = "roomId로 채팅 내용을 저장합니다."
    )
    public ResponseEntity<ApiResponseDto<ChatMessageResponse>> sendMessage(
            @Parameter(
                    description = "채팅방 ID",
                    example = "1",
                    required = true,
                    in = ParameterIn.PATH  // 이 부분이 중요!
            )
            @PathVariable Long roomId,  // "roomId" 생략 가능 (이름이 같을 때)
            @Valid @RequestBody ChatMaessageRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        ChatMessageResponse response = chatService.sendMessage(email, roomId, request);

        return ResponseEntity.ok(ApiResponseDto.of(
                200,
                "메시지 전송 성공",
                response
        ));
    }
}
