package goorm.ddok.team.service;

import goorm.ddok.badge.service.BadgeService;
import goorm.ddok.chat.dto.response.PaginationResponse;
import goorm.ddok.chat.service.ChatRoomService;
import goorm.ddok.chat.service.DmRequestCommandService;
import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamMember;
import goorm.ddok.team.domain.TeamType;
import goorm.ddok.team.dto.response.TeamApplicantUserResponse;
import goorm.ddok.team.dto.response.TeamMemberResponse;
import goorm.ddok.team.dto.response.TeamMembersResponse;
import goorm.ddok.team.repository.TeamMemberRepository;
import goorm.ddok.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamMemberQueryService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final StudyRecruitmentRepository studyRecruitmentRepository;
    private final BadgeService badgeService;
    private final ChatRoomService chatRoomService;
    private final DmRequestCommandService dmRequestService;


    /**
     * 특정 팀(teamId)의 확정된 팀원 목록을 조회합니다.
     * - 로그인한 사용자가 팀원인지 검증합니다.
     * - 승인된 멤버(LEADER / MEMBER, deletedAt == null)만 조회됩니다.
     *
     * @param teamId 조회할 팀 ID
     * @param user   현재 로그인한 사용자 정보
     * @param page   페이지 번호 (0부터 시작)
     * @param size   페이지 크기
     * @return {@link TeamMembersResponse} 팀원 목록 + 페이지네이션 정보
     */
    public TeamMembersResponse getMembers(Long teamId, CustomUserDetails user, int page, int size) {
        if (user == null || user.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        Long currentUserId = user.getId();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_NOT_FOUND));

        // 팀원 여부 확인
        boolean isTeamMember = teamMemberRepository
                .existsByTeam_IdAndUser_IdAndDeletedAtIsNull(teamId, currentUserId);

        if (!isTeamMember) {
            throw new GlobalException(ErrorCode.FORBIDDEN_TEAM_ACCESS);
        }

        String teamStatus = resolveTeamStatus(team);

        Page<TeamMember> members = teamMemberRepository
                .findByTeam_IdAndDeletedAtIsNull(teamId, PageRequest.of(page, size));

        List<TeamMemberResponse> items = members.stream()
                .map(m -> TeamMemberResponse.builder()
                        .memberId(m.getId())
                        .decidedPosition(resolveDecidedPosition(team, m))
                        .role(m.getRole().name())
                        .joinedAt(m.getCreatedAt())
                        .IsMine(m.getUser().getId().equals(currentUserId))
                        .user(toUserResponse(m.getUser(), currentUserId))
                        .build())
                .toList();

        return TeamMembersResponse.builder()
                .pagination(PaginationResponse.of(members))
                .teamId(team.getId())
                .teamType(team.getType())
                .teamTitle(team.getTitle())
                .teamStatus(teamStatus)
                .recruitmentId(team.getRecruitmentId())
                .IsLeader(team.getUser().getId().equals(currentUserId))
                .items(items)
                .build();
    }

    /**
     * 팀원의 확정 포지션명을 조회합니다.
     * - 프로젝트 팀인 경우: ProjectParticipant -> ProjectRecruitmentPosition -> positionName
     * - 스터디 팀인 경우: null 반환
     *
     * @param team   조회할 팀
     * @param member 조회할 팀 멤버
     * @return 포지션명 (스터디의 경우 null)
     */
    private String resolveDecidedPosition(Team team, TeamMember member) {
        if (team.getType() == TeamType.PROJECT) {
            return projectParticipantRepository
                    .findByPosition_ProjectRecruitment_IdAndUser_IdAndDeletedAtIsNull(
                            team.getRecruitmentId(),
                            member.getUser().getId()
                    )
                    .map(pp -> pp.getPosition().getPositionName())
                    .orElseThrow(() -> new GlobalException(ErrorCode.POSITION_NOT_FOUND));
        }
        return null; // Study는 없음
    }

    /**
     * User 엔티티를 TeamApplicantUserResponse DTO로 변환합니다.
     * - 대표 배지: "착한 배지" 중 tier가 가장 높은 것
     * - 탈주 배지: AbandonBadgeDto 조회
     *
     * @param user 변환할 사용자
     * @return {@link TeamApplicantUserResponse} 사용자 요약 정보
     */
    private TeamApplicantUserResponse toUserResponse(User user, Long currentUserId) {
        BadgeDto mainBadge = badgeService.getRepresentativeGoodBadge(user);
        AbandonBadgeDto abandonBadge = badgeService.getAbandonBadge(user);

        Long chatRoomId = null;
        boolean dmPending = false;

        if (currentUserId != null && !Objects.equals(currentUserId, user.getId())) {
            chatRoomId = chatRoomService.findPrivateRoomId(currentUserId, user.getId())
                    .orElse(null);
            dmPending = dmRequestService.isDmPendingOrAcceptedOrChatExists(currentUserId, user.getId());
        }

        return TeamApplicantUserResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .temperature(findTemperature(user))
                .mainPosition(resolvePrimaryPosition(user))
                .chatRoomId(chatRoomId)
                .dmRequestPending(dmPending)
                .mainBadge(mainBadge)
                .abandonBadge(abandonBadge)
                .build();
    }

    /**
     * User의 메인 포지션명을 조회합니다.
     *
     * @param user 사용자
     * @return 메인 포지션명 (없으면 null)
     */
    private static String resolvePrimaryPosition(User user) {
        return user.getPositions().stream()
                .filter(pos -> pos.getType() == UserPositionType.PRIMARY)
                .map(UserPosition::getPositionName)
                .findFirst()
                .orElse(null);
    }
    private BigDecimal findTemperature(User user) {
        if (user.getReputation() == null || user.getReputation().getTemperature() == null) {
            throw new GlobalException(ErrorCode.REPUTATION_NOT_FOUND);
        }
        return user.getReputation().getTemperature();
    }

    /**
     * 팀의 상태를 조회합니다.
     * - 프로젝트 팀 → ProjectRecruitment.teamStatus
     * - 스터디 팀 → StudyRecruitment.teamStatus
     */
    private String resolveTeamStatus(Team team) {
        if (team.getType() == TeamType.PROJECT) {
            return projectRecruitmentRepository.findById(team.getRecruitmentId())
                    .orElseThrow(() -> new GlobalException(ErrorCode.RECRUITMENT_NOT_FOUND))
                    .getTeamStatus()
                    .name();
        } else {
            return studyRecruitmentRepository.findById(team.getRecruitmentId())
                    .orElseThrow(() -> new GlobalException(ErrorCode.RECRUITMENT_NOT_FOUND))
                    .getTeamStatus()
                    .name();
        }
    }
}
