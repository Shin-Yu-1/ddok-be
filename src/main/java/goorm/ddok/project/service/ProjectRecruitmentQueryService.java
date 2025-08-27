package goorm.ddok.project.service;

import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.global.util.AddressNormalizer;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import goorm.ddok.member.repository.UserReputationRepository;
import goorm.ddok.project.domain.*;
import goorm.ddok.project.dto.ProjectPositionDto;
import goorm.ddok.project.dto.ProjectUserSummaryDto;
import goorm.ddok.project.dto.response.ProjectDetailResponse;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectRecruitmentQueryService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final UserReputationRepository userReputationRepository;
    private final ProjectApplicationRepository projectApplicationRepository;

    /**
     * 프로젝트 모집글 상세 조회
     */
    public ProjectDetailResponse getProjectDetail(Long projectId, CustomUserDetails userDetails) {
        // 1. 프로젝트 모집글 존재 여부 확인
        ProjectRecruitment project = projectRecruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.RECRUITMENT_NOT_FOUND));

        User currentUser = (userDetails != null) ? userDetails.getUser() : null;

        // 2. 참여자 조회 (position -> project 통해서 조회)
        List<ProjectParticipant> participants =
                projectParticipantRepository.findByPosition_ProjectRecruitment(project);

        // 3. 리더 조회
        ProjectParticipant leader = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.LEADER)
                .findFirst()
                .orElseThrow(() -> new GlobalException(ErrorCode.LEADER_NOT_FOUND));



        // 4. 지원자 수 (application 기준)
        int applicantCount = projectApplicationRepository.countByProjectRecruitment(project);

        // 5. 포지션별 현황
        List<ProjectPositionDto> positionDtos = project.getPositions().stream()
                .map(position -> {
                    long confirmedCount = participants.stream()
                            .filter(p -> p.getPosition().equals(position)
                                    && p.getRole() == ParticipantRole.MEMBER)
                            .count();

                    boolean isApplied = currentUser != null &&
                            projectApplicationRepository.findByUser_IdAndPosition_ProjectRecruitment_Id(
                                    currentUser.getId(), project.getId()
                            ).isPresent();

                    boolean isApproved = currentUser != null &&
                            participants.stream().anyMatch(p ->
                                    p.getUser().getId().equals(currentUser.getId())
                                            && p.getRole() == ParticipantRole.MEMBER
                                            && p.getPosition().equals(position)
                            );

                    boolean isAvailable = confirmedCount < project.getCapacity();

                    return ProjectPositionDto.builder()
                            .position(position.getPositionName())
                            .applied(applicantCount) // TODO: 필요하면 포지션별 지원자 수 카운트로 변경
                            .confirmed((int) confirmedCount)
                            .isApplied(isApplied)
                            .isApproved(isApproved)
                            .isAvailable(isAvailable)
                            .build();
                })
                .toList();

        // 6. 리더 DTO
        ProjectUserSummaryDto leaderDto = toUserSummaryDto(leader, currentUser);

        // 7. 참여자 DTO (리더 제외)
        List<ProjectUserSummaryDto> participantDtos = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.MEMBER)
                .map(p -> toUserSummaryDto(p, currentUser))
                .toList();

        // 8. 응답 조립
        return ProjectDetailResponse.builder()
                .projectId(project.getId())
                .isMine(currentUser != null && project.getUser().getId().equals(currentUser.getId()))
                .title(project.getTitle())
                .teamStatus(project.getTeamStatus())
                .bannerImageUrl(project.getBannerImageUrl())
                .traits(project.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .capacity(project.getCapacity())
                .applicantCount(applicantCount)
                .mode(project.getProjectMode())
                .address(resolveAddress(project))
                .preferredAges(PreferredAgesDto.builder()
                        .ageMin(project.getAgeMin())
                        .ageMax(project.getAgeMax())
                        .build())
                .expectedMonth(project.getExpectedMonths())
                .startDate(project.getStartDate())
                .detail(project.getContentMd())
                .positions(positionDtos)
                .leader(leaderDto)
                .participants(participantDtos)
                .build();
    }

    /** 주소 변환 */
    private String resolveAddress(ProjectRecruitment project) {
        if (project.getProjectMode() == ProjectMode.ONLINE) {
            return null; // ONLINE 시 null
        }

        if (project.getRegion1depthName() == null || project.getRegion2depthName() == null) {
            throw new GlobalException(ErrorCode.INVALID_LOCATION);
        }

        return AddressNormalizer.buildAddress(
                project.getRegion1depthName(),
                project.getRegion2depthName()
        );
    }


    /** UserSummary 변환 */
    private ProjectUserSummaryDto toUserSummaryDto(ProjectParticipant participant, User currentUser) {
        User user = participant.getUser();

        // 메인 포지션
        String mainPosition = user.getPositions().stream()
                .filter(pos -> pos.getType() == UserPositionType.PRIMARY)
                .map(UserPosition::getPositionName)
                .findFirst()
                .orElse(null);

        // TODO: Badge / AbandonBadge 매핑 로직 추가 필요
        return ProjectUserSummaryDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .mainPosition(mainPosition)
                .mainBadge(null)       // TODO: BadgeDto 매핑
                .abandonBadge(null)    // TODO: AbandonBadgeDto 매핑
                .temperature(36.5)     // TODO: 온도 로직 연결
                .decidedPosition(participant.getPosition().getPositionName())
                .isMine(currentUser != null && user.getId().equals(currentUser.getId()))
                .chatRoomId(null)      // TODO: 채팅 연동 후 매핑
                .dmRequestPending(false) // TODO: DM 연동 후 매핑
                .build();
    }
}