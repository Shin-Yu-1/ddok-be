package goorm.ddok.chat.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.member.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomTxHelper {
    private final ChatRoomManagementService chatRoomManagementService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPrivateRoomSafely(User from, User to) {
        try {
            chatRoomManagementService.createPrivateChatRoom(from, to);
        } catch (goorm.ddok.global.exception.GlobalException ex) {
            if (ex.getErrorCode() != ErrorCode.CHAT_ROOM_ALREADY_EXISTS) throw ex;
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // 경합으로 인해 뒤늦은 유니크 충돌(이미 방이 만들어진 경우) -> 조용히 통과
        }
    }
}
