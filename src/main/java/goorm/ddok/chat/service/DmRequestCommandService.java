package goorm.ddok.chat.service;


import goorm.ddok.chat.domain.DmRequest;
import goorm.ddok.chat.domain.DmRequestStatus;
import goorm.ddok.chat.dto.request.DmRequestDto;
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

@RequiredArgsConstructor
@Service
@Transactional
public class DmRequestCommandService {

    private final DmRequestRepository dmRequestRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;


    public DmRequestDto create(Long toUserId, CustomUserDetails loginUser) {
        if (loginUser == null || loginUser.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        Long fromUserId = loginUser.getUser().getId();
        if (fromUserId.equals(toUserId)) {
            throw new GlobalException(ErrorCode.CANNOT_DM_SELF);
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

        // 알림 이벤트 발행 (받는 사람에게 DM_REQUEST)
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
}
