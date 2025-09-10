package goorm.ddok.notification.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.response.NotificationResponse;
import goorm.ddok.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(Long userId, Boolean isRead, String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> result;
        if (type != null && !type.isBlank()) {
            final NotificationType t;
            try {
                t = NotificationType.valueOf(type);
            } catch (IllegalArgumentException e) {
                throw new GlobalException(ErrorCode.INVALID_NOTIFICATION_TYPE);
            }

            if (isRead == null) {
                result = notificationRepository.findByReceiver_IdAndType(userId, t, pageable);
            } else {
                result = notificationRepository.findByReceiver_IdAndReadAndType(userId, isRead, t, pageable);
            }
        } else {
            if (isRead == null) {
                result = notificationRepository.findByReceiver_Id(userId, pageable);
            } else {
                result = notificationRepository.findByReceiver_IdAndRead(userId, isRead, pageable);
            }
        }

        Set<Long> actorIds = result.getContent().stream()
                .map(this::extractActorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> actorMap = actorIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return result.map(n -> {
            Long actorId = extractActorId(n);
            User actor = (actorId != null) ? actorMap.get(actorId) : null;
            BigDecimal temp = (actor != null && actor.getReputation() != null)
                    ? actor.getReputation().getTemperature()
                    : null;
            return NotificationResponse.from(n, actor, temp);
        });
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByReceiver_IdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

        if (!n.getReceiver().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }
        if (Boolean.FALSE.equals(n.getRead())) {
            n.markRead();
            notificationRepository.save(n);
        }
    }

    private Long extractActorId(Notification n) {
        if (n.getType() == null) return null;
        return switch (n.getType()) {
            case PROJECT_JOIN_REQUEST, STUDY_JOIN_REQUEST -> n.getApplicantUserId();
            case PROJECT_JOIN_APPROVED, PROJECT_JOIN_REJECTED,
                 STUDY_JOIN_APPROVED, STUDY_JOIN_REJECTED -> n.getRequesterUserId();
            default -> null;
        };
    }
}
