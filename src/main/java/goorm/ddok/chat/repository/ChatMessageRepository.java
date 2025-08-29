package goorm.ddok.chat.repository;

import goorm.ddok.chat.domain.ChatMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Optional<ChatMessage> findById(Long id);

    // createdAt 기준으로 정렬해서 반환
    List<ChatMessage> findAllByRoom_IdInAndDeletedAtIsNullOrderByCreatedAtDesc(List<Long> roomIds);

    // ContentText 조회
    List<ChatMessage> findAllByRoom_IdAndContentTextContainingAndDeletedAtIsNullOrderByCreatedAtDesc(Long roomId, String contentText);

    List<ChatMessage> findAllByRoom_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Long roomId);
}