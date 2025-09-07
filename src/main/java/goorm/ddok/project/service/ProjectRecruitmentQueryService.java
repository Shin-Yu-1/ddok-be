package goorm.ddok.project.service;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.global.dto.BadgeDto;
import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import goorm.ddok.project.domain.*;
import goorm.ddok.project.dto.ProjectPositionDto;
import goorm.ddok.project.dto.ProjectUserSummaryDto;
import goorm.ddok.project.dto.response.ProjectDetailResponse;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;

import goorm.ddok.reputation.domain.UserReputation;
import goorm.ddok.reputation.repository.UserReputationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectRecruitmentQueryService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final UserReputationRepository userReputationRepository;

    /** 프로젝트 모집글 상세 조회 */
    public ProjectDetailResponse getProjectDetail(Long projectId, CustomUserDetails userDetails) {
        // 1) 프로젝트 존재 + 삭제여부 확인
        ProjectRecruitment project = projectRecruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.RECRUITMENT_NOT_FOUND));
        ensureNotDeleted(project);

        User me = (userDetails != null) ? userDetails.getUser() : null;

        // 2) 참가자 조회 (리더 포함)
        List<ProjectParticipant> participants =
                projectParticipantRepository.findByPosition_ProjectRecruitment(project);

        // 3) 리더
        ProjectParticipant leader = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.LEADER)
                .findFirst()
                .orElseThrow(() -> new GlobalException(ErrorCode.LEADER_NOT_FOUND));

        // 4) 총 지원자 수
        int applicantCount = projectApplicationRepository.countByPosition_ProjectRecruitment(project);

        // 5) 포지션별 현황
        List<ProjectPositionDto> positionDtos = project.getPositions().stream()
                .map(position -> {
                    long confirmedCount = participants.stream()
                            .filter(p -> p.getRole() == ParticipantRole.MEMBER && Objects.equals(p.getPosition(), position))
                            .count();

                    int appliedCountForPos = projectApplicationRepository.countByPosition(position);

                    boolean isApplied =
                            (me != null) &&
                            (projectApplicationRepository.existsByUser_IdAndPosition_IdAndStatus(
                                    me.getId(), position.getId(), ApplicationStatus.PENDING) ||
                            projectApplicationRepository.existsByUser_IdAndPosition_IdAndStatus(
                                    me.getId(), position.getId(), ApplicationStatus.APPROVED));

                    boolean isApproved = (me != null) &&
                            projectParticipantRepository.existsByUser_IdAndPosition_IdAndRoleAndDeletedAtIsNull(
                                    me.getId(), position.getId(), ParticipantRole.MEMBER);

                    boolean alreadyAppliedOtherPos = (me != null) &&
                            projectApplicationRepository.existsByUser_IdAndPosition_ProjectRecruitment_Id(me.getId(), project.getId())
                            && !isApplied;

                    boolean alreadyMemberAnyPos = (me != null) &&
                            projectParticipantRepository.existsByUser_IdAndPosition_ProjectRecruitment_IdAndDeletedAtIsNull(
                                    me.getId(), project.getId());

                    boolean isAvailable = (project.getTeamStatus() == TeamStatus.RECRUITING)
                            && (confirmedCount < project.getCapacity())
                            && !alreadyMemberAnyPos
                            && !alreadyAppliedOtherPos;

                    return ProjectPositionDto.builder()
                            .position(position.getPositionName())
                            .applied(appliedCountForPos)
                            .confirmed((int) confirmedCount)
                            .IsApplied(isApplied)
                            .IsApproved(isApproved)
                            .IsAvailable(isAvailable)
                            .build();
                })
                .toList();

        // 6) 리더/멤버 요약 (온도/배지/DM/채팅방: 조회 실패 시 null 폴백)
        BigDecimal leaderTemp = userReputationRepository.findByUserId(leader.getUser().getId())
                .map(UserReputation::getTemperature)
                .orElse(null);
        ProjectUserSummaryDto leaderDto = toUserSummaryDto(leader, me, leaderTemp);

        List<ProjectUserSummaryDto> memberDtos = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.MEMBER)
                .map(p -> {
                    BigDecimal temp = userReputationRepository.findByUserId(p.getUser().getId())
                            .map(UserReputation::getTemperature)
                            .orElse(null);

                    return toUserSummaryDto(p, me, temp);
                })
                .toList();

        // 7) 위치 객체 구성
        LocationDto location = buildLocationForRead(project);

        // 8) 선호 연령 (0/0이면 null)
        PreferredAgesDto ages = (project.getAgeMin() == 0 && project.getAgeMax() == 0)
                ? null
                : PreferredAgesDto.builder().ageMin(project.getAgeMin()).ageMax(project.getAgeMax()).build();

        // 9) 응답 조립
        return ProjectDetailResponse.builder()
                .projectId(project.getId())
                .IsMine(me != null && project.getUser().getId().equals(me.getId()))
                .title(project.getTitle())
                .teamStatus(project.getTeamStatus())
                .bannerImageUrl(project.getBannerImageUrl())
                .traits(project.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .capacity(project.getCapacity())
                .applicantCount(applicantCount)
                .mode(project.getProjectMode())
                .location(location)
                .preferredAges(ages)
                .expectedMonth(project.getExpectedMonths())
                .startDate(project.getStartDate())
                .detail(project.getContentMd())
                .positions(positionDtos)
                .leader(leaderDto)
                .participants(memberDtos)
                .build();
    }

    /** soft delete 확인 */
    private void ensureNotDeleted(ProjectRecruitment pr) {
        if (pr.getDeletedAt() != null) {
            throw new GlobalException(ErrorCode.RECRUITMENT_NOT_FOUND);
        }
    }

    /** UserSummary 변환 (+ 배지/DM/채팅방 기본값: null 폴백) */
    private ProjectUserSummaryDto toUserSummaryDto(ProjectParticipant participant, User currentUser, BigDecimal temperature) {
        User user = participant.getUser();

        String mainPosition = user.getPositions().stream()
                .filter(pos -> pos.getType() == UserPositionType.PRIMARY)
                .map(UserPosition::getPositionName)
                .findFirst()
                .orElse(null);

        Long meId = (currentUser != null) ? currentUser.getId() : null;
        Long otherId = user.getId();

        BadgeDto mainBadge = resolveMainBadge(user);
        AbandonBadgeDto abandonBadge = resolveAbandonBadge(user);
        Long chatRoomId = resolveChatRoomId(meId, otherId);
        boolean dmPending = resolveDmPending(meId, otherId);

        return ProjectUserSummaryDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .mainPosition(mainPosition)
                .mainBadge(mainBadge)
                .abandonBadge(abandonBadge)
                .temperature(temperature)
                .decidedPosition(participant.getPosition().getPositionName())
                .IsMine(currentUser != null && user.getId().equals(currentUser.getId()))
                .chatRoomId(chatRoomId)
                .dmRequestPending(dmPending)
                .build();
    }

    private LocationDto buildLocationForRead(ProjectRecruitment pr) {
        if (pr.getProjectMode() == ProjectMode.online) return null;
        String full = composeFullAddress(
                pr.getRegion1depthName(), pr.getRegion2depthName(), pr.getRegion3depthName(),
                pr.getRoadName(), pr.getMainBuildingNo(), pr.getSubBuildingNo()
        );
        return LocationDto.builder()
                .address(full)
                .region1depthName(pr.getRegion1depthName())
                .region2depthName(pr.getRegion2depthName())
                .region3depthName(pr.getRegion3depthName())
                .roadName(pr.getRoadName())
                .mainBuildingNo(pr.getMainBuildingNo())
                .subBuildingNo(pr.getSubBuildingNo())
                .zoneNo(pr.getZoneNo())
                .latitude(pr.getLatitude())
                .longitude(pr.getLongitude())
                .build();
    }

    private String composeFullAddress(String r1, String r2, String r3, String road, String main, String sub) {
        StringBuilder sb = new StringBuilder();
        if (r1 != null && !r1.isBlank()) sb.append(r1).append(" ");
        if (r2 != null && !r2.isBlank()) sb.append(r2).append(" ");
        if (r3 != null && !r3.isBlank()) sb.append(r3).append(" ");
        if (road != null && !road.isBlank()) sb.append(road).append(" ");
        if (main != null && !main.isBlank()) {
            sb.append(main);
            if (sub != null && !sub.isBlank()) sb.append("-").append(sub);
        }
        String s = sb.toString().trim().replaceAll("\\s+", " ");
        return s.isBlank() ? null : s;
    }

    // ==== 배지/DM/채팅방 기본 구현 (실서비스 연동 지점; 현재는 null 폴백) ====
    private BadgeDto resolveMainBadge(User user) { return null; }
    private AbandonBadgeDto resolveAbandonBadge(User user) { return null; }
    private Long resolveChatRoomId(Long meId, Long otherId) { return null; }
    private boolean resolveDmPending(Long meId, Long otherId) { return false; }
}