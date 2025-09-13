package goorm.ddok.notification.repository;

import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiver_Id(Long receiverId, Pageable pageable);

    Page<Notification> findByReceiver_IdAndRead(Long receiverId, Boolean read, Pageable pageable);

    Page<Notification> findByReceiver_IdAndType(Long receiverId, NotificationType type, Pageable pageable);

    Page<Notification> findByReceiver_IdAndReadAndType(Long receiverId, Boolean read, NotificationType type, Pageable pageable);

    long countByReceiver_IdAndReadFalse(Long receiverId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update Notification n
         set n.processed = true,
             n.processedAt = :now
       where n.receiver.id = :receiverId
         and n.type = :type
         and (:projectId is null or n.projectId = :projectId)
         and (:studyId   is null or n.studyId   = :studyId)
         and (:applicantId is null or n.applicantUserId = :applicantId)
         and n.processed = false
    """)
    int markProcessedByContext(
            @Param("receiverId") Long receiverId,
            @Param("type") NotificationType type,
            @Param("projectId") Long projectId,
            @Param("studyId") Long studyId,
            @Param("applicantId") Long applicantId,
            @Param("now") Instant now
    );
}
