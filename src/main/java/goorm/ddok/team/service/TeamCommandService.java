package goorm.ddok.team.service;

import goorm.ddok.chat.service.ChatRoomManagementService;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.project.domain.ProjectParticipant;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.study.domain.StudyParticipant;
import goorm.ddok.study.repository.StudyApplicationRepository;
import goorm.ddok.study.repository.StudyParticipantRepository;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamMember;
import goorm.ddok.team.domain.TeamMemberRole;
import goorm.ddok.team.domain.TeamType;
import goorm.ddok.team.repository.TeamMemberRepository;
import goorm.ddok.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamCommandService {

    private final ChatRoomManagementService chatRoomManagementService;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final StudyParticipantRepository studyParticipantRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final StudyApplicationRepository studyApplicationRepository;

    /**
     * 모집글 기반 팀 생성 (프로젝트 / 스터디 공용)
     */
    @Transactional
    public Team createTeamForRecruitment(Long recruitmentId, TeamType type, String title, User leader) {
        Team team = Team.builder()
                .recruitmentId(recruitmentId)
                .type(type)
                .title(title)
                .user(leader)
                .build();

        // 리더 팀원 자동 추가
        team.addMember(leader, TeamMemberRole.LEADER);
        Team savedTeam = teamRepository.save(team);
        // 팀 채팅 추가
        chatRoomManagementService.createTeamChatRoom(savedTeam, leader);

        return savedTeam;
    }

    /**
     * 팀원 추방 처리 (Soft Delete)
     *
     * @param teamId   팀 ID
     * @param memberId 추방할 팀원 ID
     * @param user     현재 로그인한 사용자 (리더만 가능)
     */
    @Transactional
    public void expelMember(Long teamId, Long memberId, CustomUserDetails user) {
        if (user == null || user.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_NOT_FOUND));

        // 리더 권한 확인
        if (!team.getUser().getId().equals(user.getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN_LEADER_ONLY);
        }

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        // 같은 팀 소속인지 검증
        if (!member.getTeam().getId().equals(teamId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN_ACTION);
        }

        // 리더는 추방할 수 없음
        if (member.getRole() == TeamMemberRole.LEADER) {
            throw new GlobalException(ErrorCode.LEADER_CANNOT_BE_EXPELLED);
        }

        // 이미 추방/탈퇴 처리된 경우
        if (member.getDeletedAt() != null) {
            throw new GlobalException(ErrorCode.ALREADY_EXPELLED);
        }

        // Soft Delete
        member.expel();
        // 채팅방 멤버 삭제
        chatRoomManagementService.removeMemberFromTeamChat(teamId, team.getUser().getId());

        openReapplyWindow(team, member.getUser().getId());

        // 모집글 참가자 동기화
        if (team.getType() == TeamType.PROJECT) {
            projectParticipantRepository.findByPosition_ProjectRecruitment_IdAndUser_IdAndDeletedAtIsNull(
                    team.getRecruitmentId(), member.getUser().getId()
            ).ifPresent(ProjectParticipant::expel);
        } else if (team.getType() == TeamType.STUDY) {
            studyParticipantRepository.findByStudyRecruitment_IdAndUser_IdAndDeletedAtIsNull(
                    team.getRecruitmentId(), member.getUser().getId()
            ).ifPresent(StudyParticipant::expel);
        }
    }

    /**
     * 팀원 중도 하차 처리 (Soft Delete)
     *
     * @param teamId      팀 ID
     * @param memberId    하차할 팀원 ID
     * @param user        현재 로그인한 사용자
     * @param confirmText 확인 문구 ("하차합니다.")
     */
    @Transactional
    public void withdrawMember(Long teamId, Long memberId, CustomUserDetails user, String confirmText) {
        if (user == null || user.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        if (!"하차합니다.".equals(confirmText)) {
            throw new GlobalException(ErrorCode.INVALID_CONFIRM_TEXT);
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_NOT_FOUND));

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_MEMBER_NOT_FOUND));

        // 본인만 하차 가능
        if (!member.getUser().getId().equals(user.getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // 리더는 하차 불가
        if (member.getRole() == TeamMemberRole.LEADER) {
            throw new GlobalException(ErrorCode.LEADER_CANNOT_WITHDRAW);
        }

        if (member.getDeletedAt() != null) {
            throw new GlobalException(ErrorCode.ALREADY_EXPELLED);
        }

        // Soft Delete
        member.expel();
        // 채팅방 멤버 삭제
        chatRoomManagementService.removeMemberFromTeamChat(teamId, team.getUser().getId());

        openReapplyWindow(team, member.getUser().getId());

        if (team.getType() == TeamType.PROJECT) {
            projectParticipantRepository.findByPosition_ProjectRecruitment_IdAndUser_IdAndDeletedAtIsNull(
                    team.getRecruitmentId(), member.getUser().getId()
            ).ifPresent(ProjectParticipant::expel);
        } else if (team.getType() == TeamType.STUDY) {
            studyParticipantRepository.findByStudyRecruitment_IdAndUser_IdAndDeletedAtIsNull(
                    team.getRecruitmentId(), member.getUser().getId()
            ).ifPresent(StudyParticipant::expel);
        }
    }

    private void openReapplyWindow(Team team, Long userId) {
        int changed = 0;
        if (team.getType() == TeamType.PROJECT) {
            changed = projectApplicationRepository
                    .markApprovedAsRejectedByRecruitmentAndUser(team.getRecruitmentId(), userId);
        } else if (team.getType() == TeamType.STUDY) {
            changed = studyApplicationRepository
                    .markApprovedAsRejectedByRecruitmentAndUser(team.getRecruitmentId(), userId);
        }
    }

}
