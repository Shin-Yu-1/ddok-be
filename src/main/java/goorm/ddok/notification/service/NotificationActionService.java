package goorm.ddok.notification.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
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
    private final TeamApplicantCommandService teamApplicantCommandService;

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

        // 수신자 확인
        if (!n.getReceiver().getId().equals(user.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }
        if (Boolean.TRUE.equals(n.getProcessed())) {
            throw new GlobalException(ErrorCode.ALREADY_PROCESSED_NOTIFICATION);
        }

        // 타입 분기
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

        } else {
            // 알림 액션이 가능한 타입만 허용
            throw new GlobalException(ErrorCode.INVALID_NOTIFICATION_ACTION);
        }

        // 알림 마킹
        n.setProcessed(true);
        n.setProcessedAt(Instant.now());
        n.setRead(true);
        notificationRepository.save(n);
    }

    private Long resolveTeamId(Notification n, TeamType type, Long recruitmentId) {
        if (n.getTeamId() != null) return n.getTeamId();

        Team team = teamRepository.findByTypeAndRecruitmentId(type, recruitmentId)
                .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_NOT_FOUND));
        // (원하면 n.setTeamId(team.getId()) 후 저장)
        return team.getId();
    }
}
