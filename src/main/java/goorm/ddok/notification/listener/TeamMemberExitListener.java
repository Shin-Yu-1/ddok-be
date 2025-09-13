package goorm.ddok.notification.listener;

import goorm.ddok.member.domain.User;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.TeamMemberExitEvent;
import goorm.ddok.notification.repository.NotificationRepository;
import goorm.ddok.notification.service.NotificationPushService;
import goorm.ddok.notification.support.NotificationMessageHelper;
import goorm.ddok.team.domain.TeamMember;
import goorm.ddok.team.repository.TeamMemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamMemberExitListener {

    private final TeamMemberRepository teamMemberRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final NotificationMessageHelper messageHelper;

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TeamMemberExitEvent e) {
        User actorRef = em.getReference(User.class, e.actorUserId());
        String actorNick = actorRef.getNickname();
        var actorTemp = (actorRef.getReputation() != null)
                ? actorRef.getReputation().getTemperature()
                : null;

        // 남은 팀원(soft delete 되지 않은 멤버), actor 제외
        List<TeamMember> receivers = teamMemberRepository.findAllByTeam_IdAndDeletedAtIsNull(e.teamId())
                .stream()
                .filter(m -> !m.getUser().getId().equals(e.actorUserId()))
                .toList();

        if (receivers.isEmpty()) return;

        // 메시지 구성 + 온도 접미사
        String actionKo = (e.reason() == TeamMemberExitEvent.Reason.WITHDRAWN) ? "추방되었습니다." : "하차했습니다.";
        String base = String.format("%s님이 \"%s\" 팀에서 %s", actorNick, e.teamTitle(), actionKo);
        String msg = messageHelper.withTemperatureSuffix(e.actorUserId(), base);

        // 알림 저장 + 푸시
        for (TeamMember r : receivers) {
            User receiverRef = em.getReference(User.class, r.getUser().getId());

            Notification noti = Notification.builder()
                    .receiver(receiverRef)
                    .type(NotificationType.TEAM_MEMBER_VIOLATION)
                    .message(msg)
                    .read(false)
                    .processed(false)
                    .createdAt(Instant.now())
                    .teamId(e.teamId())
                    .teamName(e.teamTitle())
                    .projectId(e.teamType() == goorm.ddok.team.domain.TeamType.PROJECT ? e.recruitmentId() : null)
                    .studyId(e.teamType() == goorm.ddok.team.domain.TeamType.STUDY ? e.recruitmentId() : null)
                    .applicantUserId(e.actorUserId())
                    .build();

            noti = notificationRepository.save(noti);

            var payload = NotificationPayload.builder()
                    .id(String.valueOf(noti.getId()))
                    .type(NotificationType.TEAM_MEMBER_VIOLATION.name())
                    .message(msg)
                    .IsRead(false)
                    .createdAt(noti.getCreatedAt())
                    .IsProcessed(false)
                    .actorUserId(String.valueOf(e.actorUserId()))
                    .actorNickname(actorNick)
                    .actorTemperature(actorTemp)
                    .userId(String.valueOf(e.actorUserId()))
                    .userNickname(actorNick)
                    .teamId(String.valueOf(e.teamId()))
                    .teamName(e.teamTitle())
                    .projectId(e.teamType() == goorm.ddok.team.domain.TeamType.PROJECT ? String.valueOf(e.recruitmentId()) : null)
                    .projectTitle(e.teamType() == goorm.ddok.team.domain.TeamType.PROJECT ? e.teamTitle() : null)
                    .studyId(e.teamType() == goorm.ddok.team.domain.TeamType.STUDY ? String.valueOf(e.recruitmentId()) : null)
                    .studyTitle(e.teamType() == goorm.ddok.team.domain.TeamType.STUDY ? e.teamTitle() : null)
                    .build();

            // 네 서비스 시그니처에 맞춤
            pushService.pushToUser(r.getUser().getId(), payload);
        }
    }
}

