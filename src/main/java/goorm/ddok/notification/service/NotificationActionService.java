package goorm.ddok.notification.service;

import goorm.ddok.chat.domain.DmRequest;
import goorm.ddok.chat.domain.DmRequestStatus;
import goorm.ddok.chat.repository.DmRequestRepository;
import goorm.ddok.chat.service.ChatRoomManagementService;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.repository.NotificationRepository;
import goorm.ddok.project.domain.ProjectApplication;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.study.domain.ApplicationStatus;
import goorm.ddok.study.domain.StudyApplication;
import goorm.ddok.study.repository.StudyApplicationRepository;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamType;
import goorm.ddok.team.repository.TeamRepository;
import goorm.ddok.team.service.TeamApplicantCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationActionService {

    private final NotificationRepository notificationRepository;
    private final TeamRepository teamRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final DmRequestRepository dmRequestRepository;
    private final TeamApplicantCommandService teamApplicantCommandService;
    private final UserRepository userRepository;
    private final ChatRoomManagementService chatRoomManagementService;
    private final ApplicationEventPublisher eventPublisher;


    public void accept(Long notificationId, CustomUserDetails user) {
        handle(notificationId, user, true);
    }

    public void reject(Long notificationId, CustomUserDetails user) {
        handle(notificationId, user, false);
    }

    private void handle(Long notificationId, CustomUserDetails user, boolean accept) {
        if (user == null || user.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);

        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!n.getReceiver().getId().equals(user.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }
        if (Boolean.TRUE.equals(n.getProcessed())) {
            throw new GlobalException(ErrorCode.ALREADY_PROCESSED_NOTIFICATION);
        }

        if (n.getType() == NotificationType.STUDY_JOIN_REQUEST) {
            Long teamId = resolveTeamId(n, TeamType.STUDY, n.getStudyId());
            StudyApplication app = studyApplicationRepository
                    .findByUser_IdAndStudyRecruitment_IdAndApplicationStatus(
                            n.getApplicantUserId(), n.getStudyId(), ApplicationStatus.PENDING)
                    .orElseThrow(() -> new GlobalException(ErrorCode.APPLICATION_NOT_FOUND));

            if (accept) teamApplicantCommandService.approve(teamId, app.getId(), user);
            else        teamApplicantCommandService.reject(teamId, app.getId(), user);

        } else if (n.getType() == NotificationType.PROJECT_JOIN_REQUEST) {
            Long teamId = resolveTeamId(n, TeamType.PROJECT, n.getProjectId());
            ProjectApplication app = projectApplicationRepository
                    .findByUser_IdAndPosition_ProjectRecruitment_IdAndStatus(
                            n.getApplicantUserId(), n.getProjectId(),
                            goorm.ddok.project.domain.ApplicationStatus.PENDING)
                    .orElseThrow(() -> new GlobalException(ErrorCode.APPLICATION_NOT_FOUND));

            if (accept) teamApplicantCommandService.approve(teamId, app.getId(), user);
            else        teamApplicantCommandService.reject(teamId, app.getId(), user);

        } else if (n.getType() == NotificationType.DM_REQUEST) {
            if (accept) acceptDm(n, user);
            else        rejectDm(n, user);

        } else {
            throw new GlobalException(ErrorCode.INVALID_NOTIFICATION_ACTION);
        }

        n.setProcessed(true);
        n.setProcessedAt(Instant.now());
        n.setRead(true);
        notificationRepository.save(n);
    }

    private Long resolveTeamId(Notification n, TeamType type, Long recruitmentId) {
        if (n.getTeamId() != null) return n.getTeamId();
        Team team = teamRepository.findByTypeAndRecruitmentId(type, recruitmentId)
                .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_NOT_FOUND));
        return team.getId();
    }

    private void acceptDm(Notification n, CustomUserDetails currentUser) {
        Long fromUserId = n.getApplicantUserId();
        if (fromUserId == null) throw new GlobalException(ErrorCode.NOTIFICATION_NOT_FOUND);

        Long toUserId = currentUser.getId();
        User from = userRepository.getReferenceById(fromUserId);
        User to   = userRepository.getReferenceById(toUserId);

        var dmRequest = dmRequestRepository
                .findTopByFromUser_IdAndToUser_IdAndStatusOrderByCreatedAtDesc(fromUserId, toUserId, DmRequestStatus.PENDING)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOTIFICATION_NOT_FOUND));

        dmRequest.accept();
        dmRequestRepository.save(dmRequest);

        try {
            chatRoomManagementService.createPrivateChatRoom(from, to);
        } catch (GlobalException ex) {
            if (ex.getErrorCode() != ErrorCode.CHAT_ROOM_ALREADY_EXISTS) throw ex;
        }

        eventPublisher.publishEvent(
                goorm.ddok.notification.event.DmRequestDecisionEvent.builder()
                        .approverUserId(toUserId)
                        .requesterUserId(fromUserId)
                        .decision("accept")
                        .notificationId(n.getId())
                        .build()
        );
    }

    private void rejectDm(Notification n, CustomUserDetails currentUser) {
        Long fromUserId = n.getApplicantUserId();
        Long toUserId = currentUser.getId();

        var dmRequest = dmRequestRepository
                .findTopByFromUser_IdAndToUser_IdAndStatusOrderByCreatedAtDesc(fromUserId, toUserId, DmRequestStatus.PENDING)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOTIFICATION_NOT_FOUND));

        dmRequest.reject();
        dmRequestRepository.save(dmRequest);

        eventPublisher.publishEvent(
                goorm.ddok.notification.event.DmRequestDecisionEvent.builder()
                        .approverUserId(toUserId)
                        .requesterUserId(fromUserId)
                        .decision("reject")
                        .notificationId(n.getId())
                        .build()
        );
    }
}
