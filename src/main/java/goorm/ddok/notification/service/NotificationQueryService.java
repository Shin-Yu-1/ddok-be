package goorm.ddok.notification.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.response.NotificationResponse;
import goorm.ddok.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(Long userId, Boolean isRead, String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> result;
        if (type != null && !type.isBlank()) {
            NotificationType t;
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

        return result.map(NotificationResponse::from);
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
        if (!Boolean.TRUE.equals(n.getRead())) {
            n.markRead();
            notificationRepository.save(n);
        }
    }
}
