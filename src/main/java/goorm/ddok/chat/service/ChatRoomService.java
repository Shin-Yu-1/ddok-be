package goorm.ddok.chat.service;

import goorm.ddok.chat.domain.ChatRoom;
import goorm.ddok.chat.domain.ChatRoomType;
import goorm.ddok.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public Optional<Long> findPrivateRoomId(Long meId, Long otherUserId) {
        return chatRoomRepository.findPrivateRoomBetweenUsers(
                ChatRoomType.PRIVATE, meId, otherUserId
        ).map(ChatRoom::getId);
    }
}
