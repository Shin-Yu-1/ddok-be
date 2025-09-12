package goorm.ddok.team.service;

import goorm.ddok.chat.service.ChatRoomManagementService;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.event.ProjectJoinApprovedEvent;
import goorm.ddok.notification.event.ProjectJoinRejectedEvent;
import goorm.ddok.notification.event.StudyJoinApprovedEvent;
import goorm.ddok.notification.event.StudyJoinRejectedEvent;
import goorm.ddok.notification.repository.NotificationRepository;
import goorm.ddok.project.domain.ProjectApplication;
import goorm.ddok.project.domain.ProjectParticipant;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.study.domain.ParticipantRole;
import goorm.ddok.study.domain.StudyApplication;
import goorm.ddok.study.domain.StudyParticipant;
import goorm.ddok.study.domain.StudyRecruitment;
import goorm.ddok.study.repository.StudyApplicationRepository;
import goorm.ddok.study.repository.StudyParticipantRepository;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamMemberRole;
import goorm.ddok.team.domain.TeamType;
import goorm.ddok.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamApplicantCommandService {

    private final StudyApplicationRepository studyApplicationRepository;
    private final StudyParticipantRepository studyParticipantRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final TeamRepository teamRepository;
    private final ChatRoomManagementService chatRoomManagementService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationRepository notificationRepository;

    /**
     * 신청 승인 처리
     *
     * @param teamId        승인할 팀 ID
     * @param applicationId 신청 ID
     * @param user          현재 로그인한 사용자 (리더만 가능)
     */
    public void approve(Long teamId, Long applicationId, CustomUserDetails user) {
        Team team = validateLeader(teamId, user);

        if (team.getType() == TeamType.STUDY) {
            // 0) 이벤트/알림에 쓸 신청 엔티티 사전 로드 (approveStudyApplication은 void)
            StudyApplication app = studyApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

            // 1) 도메인 승인 처리 (기존 메서드 그대로 사용)
            approveStudyApplication(team, applicationId);

            // 2) 승인 이벤트 발행 → 신청자에게 "STUDY_JOIN_APPROVED" 푸시
            eventPublisher.publishEvent(
                    StudyJoinApprovedEvent.builder()
                            .applicantUserId(app.getUser().getId())
                            .studyId(app.getStudyRecruitment().getId())
                            .studyTitle(app.getStudyRecruitment().getTitle())
                            .approverUserId(user.getUser().getId())
                            .build()
            );

            // 3) 리더에게 도착해 있던 STUDY_JOIN_REQUEST 알림 processed 처리
            markRequestNotificationProcessedForStudy(team, app);

        } else if (team.getType() == TeamType.PROJECT) {
            // 0) 이벤트/알림에 쓸 신청 엔티티 사전 로드
            ProjectApplication app = projectApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

            // 1) 도메인 승인 처리
            approveProjectApplication(team, applicationId);

            // 2) 승인 이벤트 발행 → 신청자에게 "PROJECT_JOIN_APPROVED" 푸시
            eventPublisher.publishEvent(
                    ProjectJoinApprovedEvent.builder()
                            .applicantUserId(app.getUser().getId())
                            .projectId(app.getPosition().getProjectRecruitment().getId())
                            .projectTitle(app.getPosition().getProjectRecruitment().getTitle())
                            .approverUserId(user.getUser().getId())
                            .applicationId(app.getId())
                            .build()
            );

            // 3) 리더에게 도착해 있던 PROJECT_JOIN_REQUEST 알림 processed 처리
            markRequestNotificationProcessedForProject(team, app);

        } else {
            throw new GlobalException(ErrorCode.INVALID_TEAM_TYPE);
        }
    }

    /**
     * 신청 거절 처리
     *
     * @param teamId        거절할 팀 ID
     * @param applicationId 신청 ID
     * @param user          현재 로그인한 사용자 (리더만 가능)
     */
    public void reject(Long teamId, Long applicationId, CustomUserDetails user) {
        Team team = validateLeader(teamId, user);

        if (team.getType() == TeamType.STUDY) {
            StudyApplication app = studyApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

            rejectStudyApplication(applicationId);

            eventPublisher.publishEvent(
                    StudyJoinRejectedEvent.builder()
                            .applicantUserId(app.getUser().getId())
                            .studyId(app.getStudyRecruitment().getId())
                            .studyTitle(app.getStudyRecruitment().getTitle())
                            .rejectorUserId(user.getUser().getId())
                            .applicationId(app.getId())
                            .build()
            );

            markRequestNotificationProcessedForStudy(team, app);

        } else if (team.getType() == TeamType.PROJECT) {
            ProjectApplication app = projectApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

            rejectProjectApplication(applicationId);

            eventPublisher.publishEvent(
                    ProjectJoinRejectedEvent.builder()
                            .applicantUserId(app.getUser().getId())
                            .projectId(app.getPosition().getProjectRecruitment().getId())
                            .projectTitle(app.getPosition().getProjectRecruitment().getTitle())
                            .rejectorUserId(user.getUser().getId())
                            .applicationId(app.getId())
                            .build()
            );

            markRequestNotificationProcessedForProject(team, app);

        } else {
            throw new GlobalException(ErrorCode.INVALID_TEAM_TYPE);
        }
    }

    @Transactional
    protected void markRequestNotificationProcessedForStudy(Team team, StudyApplication app) {
        notificationRepository.markProcessedByContext(
                team.getUser().getId(),
                NotificationType.STUDY_JOIN_REQUEST,
                null,
                app.getStudyRecruitment().getId(),
                app.getUser().getId(),
                Instant.now()
        );
    }

    @Transactional
    protected void markRequestNotificationProcessedForProject(Team team, ProjectApplication app) {
        notificationRepository.markProcessedByContext(
                team.getUser().getId(),
                NotificationType.PROJECT_JOIN_REQUEST,
                app.getPosition().getProjectRecruitment().getId(),
                null,
                app.getUser().getId(),
                Instant.now()
        );
    }

    /**
     * 리더 권한 검증
     *
     * @param teamId 검증할 팀 ID
     * @param user   현재 로그인한 사용자
     * @return 리더 권한이 확인된 Team 엔티티
     */
    private Team validateLeader(Long teamId, CustomUserDetails user) {
        if (user == null || user.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_NOT_FOUND));

        if (!team.getUser().getId().equals(user.getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN_LEADER_ONLY);
        }
        return team;
    }

    /**
     * 스터디 신청 승인 처리
     *
     * @param team          승인 대상 팀
     * @param applicationId 신청 ID
     */
    private void approveStudyApplication(Team team, Long applicationId) {
        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.isPending()) {
            throw new GlobalException(ErrorCode.ALREADY_PROCESSED_APPLICATION);
        }

        StudyRecruitment recruitment = application.getStudyRecruitment();

        long participantCount =
                studyParticipantRepository.countByStudyRecruitment_IdAndDeletedAtIsNull(recruitment.getId());
        if (participantCount >= recruitment.getCapacity() + 1) {
            throw new GlobalException(ErrorCode.CAPACITY_EXCEEDED);
        }

        application.approve();

        StudyParticipant participant = StudyParticipant.builder()
                .studyRecruitment(recruitment)
                .user(application.getUser())
                .role(ParticipantRole.MEMBER)
                .build();
        studyParticipantRepository.save(participant);

        team.addMember(application.getUser(), TeamMemberRole.MEMBER);
        chatRoomManagementService.addMemberToTeamChat(team, participant.getUser(), TeamMemberRole.MEMBER);
    }

    /**
     * 스터디 신청 거절 처리
     *
     * @param applicationId 신청 ID
     */
    private void rejectStudyApplication(Long applicationId) {
        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.isPending()) {
            throw new GlobalException(ErrorCode.ALREADY_PROCESSED_APPLICATION);
        }

        application.reject();
    }

    /**
     * 프로젝트 신청 승인 처리
     *
     * @param team          승인 대상 팀
     * @param applicationId 신청 ID
     */
    private void approveProjectApplication(Team team, Long applicationId) {
        ProjectApplication application = projectApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.isPending()) {
            throw new GlobalException(ErrorCode.ALREADY_PROCESSED_APPLICATION);
        }

        ProjectRecruitment recruitment = application.getPosition().getProjectRecruitment();
        long participantCount =
                projectParticipantRepository.countByPosition_ProjectRecruitment_IdAndDeletedAtIsNull(recruitment.getId());

        if (participantCount >= recruitment.getCapacity() + 1) {
            throw new GlobalException(ErrorCode.CAPACITY_EXCEEDED);
        }

        application.approve();

        ProjectParticipant participant = ProjectParticipant.builder()
                .user(application.getUser())
                .position(application.getPosition())
                .role(goorm.ddok.project.domain.ParticipantRole.MEMBER)
                .build();
        projectParticipantRepository.save(participant);

        team.addMember(application.getUser(), TeamMemberRole.MEMBER);
        chatRoomManagementService.addMemberToTeamChat(team, participant.getUser(), TeamMemberRole.MEMBER);
    }

    /**
     * 프로젝트 신청 거절 처리
     *
     * @param applicationId 신청 ID
     */
    private void rejectProjectApplication(Long applicationId) {
        ProjectApplication application = projectApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.isPending()) {
            throw new GlobalException(ErrorCode.ALREADY_PROCESSED_APPLICATION);
        }

        application.reject();
    }
}