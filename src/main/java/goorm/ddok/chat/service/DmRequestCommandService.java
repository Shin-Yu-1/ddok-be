package goorm.ddok.chat.service;


import goorm.ddok.chat.domain.DmRequest;
import goorm.ddok.chat.domain.DmRequestStatus;
import goorm.ddok.chat.dto.request.DmRequestDto;
import goorm.ddok.chat.repository.ChatRepository;
import goorm.ddok.chat.repository.DmRequestRepository;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.notification.event.DmRequestCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DmRequestCommandService {

    private final DmRequestRepository dmRequestRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatRepository chatRepository;
    private final ChatRoomService chatRoomService;

    private static final Set<DmRequestStatus> ACTIVE_STATUSES =
            EnumSet.of(DmRequestStatus.PENDING, DmRequestStatus.ACCEPTED);

    public DmRequestDto create(Long toUserId, CustomUserDetails loginUser) {
        if (loginUser == null || loginUser.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        Long fromUserId = loginUser.getUser().getId();
        if (fromUserId.equals(toUserId)) {
            throw new GlobalException(ErrorCode.CANNOT_DM_SELF);
        }

        boolean hasPrivateRoom =
                chatRepository.existsPrivateRoomByUserIds(fromUserId, toUserId)
        || chatRepository.existsPrivateRoomByUserIds(toUserId, fromUserId);

        if (hasPrivateRoom) {
            throw new GlobalException(ErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }

        User from = userRepository.findById(fromUserId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
        User to = userRepository.findById(toUserId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        if (dmRequestRepository.existsPendingBetween(fromUserId, toUserId, DmRequestStatus.PENDING)) {
            throw new GlobalException(ErrorCode.DM_REQUEST_ALREADY_PENDING);
        }

        DmRequest saved = dmRequestRepository.save(
                DmRequest.builder()
                        .fromUser(from)
                        .toUser(to)
                        .status(DmRequestStatus.PENDING)
                        .build()
        );

        eventPublisher.publishEvent(
                DmRequestCreatedEvent.builder()
                        .dmRequestId(saved.getId())
                        .fromUserId(from.getId())
                        .fromNickname(from.getNickname())
                        .toUserId(to.getId())
                        .build()
        );

        return DmRequestDto.from(saved);
    }

    public boolean isDmPendingOrAcceptedOrChatExists(Long meId, Long otherId) {
        if (meId == null || otherId == null || Objects.equals(meId, otherId)) return false;

        if (chatRoomService.findPrivateRoomId(meId, otherId).isPresent()) {
            return true;
        }

        return dmRequestRepository.existsEitherDirectionWithStatuses(meId, otherId, ACTIVE_STATUSES);
    }
}
