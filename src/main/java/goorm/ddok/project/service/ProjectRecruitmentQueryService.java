package goorm.ddok.project.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPositionType;
import goorm.ddok.member.domain.UserReputation;
import goorm.ddok.member.repository.UserReputationRepository;
import goorm.ddok.project.domain.ParticipantRole;
import goorm.ddok.project.domain.ProjectParticipant;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.domain.ProjectRecruitmentTrait;
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
public class ProjectRecruitmentReadService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final ProjectParticipantRepository participantRepository;
    private final UserReputationRepository userReputationRepository;
    private final ProjectApplicationRepository projectApplicationRepository;

    /**
     * 프로젝트 모집글 상세 조회
     */
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectDetail(Long projectId, CustomUserDetails userDetails) {
        // 1. 모집글 조회
        ProjectRecruitment recruitment = projectRecruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

        User currentUser = userDetails != null ? userDetails.getUser() : null;
        boolean isMine = currentUser != null && recruitment.getUser().getId().equals(currentUser.getId());

        // 2. 신청자 수
        int applicantCount = projectApplicationRepository.countByProjectRecruitment(recruitment);

        // 3. 포지션별 현황 (임시, 나중에 실제 지원/참여 데이터 반영)
        List<ProjectDetailResponse.PositionDto> positions = recruitment.getPositions().stream()
                .map(pos -> ProjectDetailResponse.PositionDto.builder()
                        .position(pos.getPositionName())
                        .applied(0) // TODO: 지원자 수
                        .confirmed(0) // TODO: 확정 인원 수
                        .isApplied(false) // TODO: 현재 유저 지원 여부
                        .isApproved(false) // TODO: 현재 유저 승인 여부
                        .isAvailable(true) // TODO: 지원 가능 여부 로직
                        .build()
                ).toList();

        // 4. 리더 정보
        ProjectParticipant leaderEntity = participantRepository.findLeaderByProjectId(recruitment.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        User leaderUser = leaderEntity.getUser();

        String leaderMainPosition = leaderUser.getPositions().stream()
                .filter(pos -> pos.getType() == UserPositionType.PRIMARY)
                .map(up -> up.getPositionName())
                .findFirst()
                .orElse(null);

        Double leaderTemperature = userReputationRepository.findByUserId(leaderUser.getId())
                .map(UserReputation::getTemperature)
                .orElse(36.5);

        ProjectDetailResponse.ParticipantDto leader = ProjectDetailResponse.ParticipantDto.builder()
                .userId(leaderUser.getId())
                .nickname(leaderUser.getNickname())
                .profileImageUrl(leaderUser.getProfileImageUrl())
                .mainPosition(leaderMainPosition)
                .temperature(leaderTemperature)
                .decidedPosition(leaderEntity.getPosition().getPositionName())
                .chatRoomId(null) // TODO: 채팅방 연동 시 구현
                .dmRequestPending(false) // TODO: DM 요청 여부
                .build();

        // 5. 참여자 정보
        List<ProjectDetailResponse.ParticipantDto> participants = participantRepository.findByProjectRecruitment(recruitment).stream()
                .filter(p -> p.getRole() != ParticipantRole.LEADER)
                .map(p -> {
                    User user = p.getUser();

                    String mainPosition = user.getPositions().stream()
                            .filter(pos -> pos.getType() == UserPositionType.PRIMARY)
                            .map(up -> up.getPositionName())
                            .findFirst()
                            .orElse(null);

                    Double temperature = userReputationRepository.findByUserId(user.getId())
                            .map(UserReputation::getTemperature)
                            .orElse(36.5);

                    return ProjectDetailResponse.ParticipantDto.builder()
                            .userId(user.getId())
                            .nickname(user.getNickname())
                            .profileImageUrl(user.getProfileImageUrl())
                            .mainPosition(mainPosition)
                            .temperature(temperature)
                            .decidedPosition(p.getPosition().getPositionName())
                            .chatRoomId(null) // TODO
                            .dmRequestPending(false) // TODO
                            .build();
                })
                .toList();

        // 6. 최종 응답 DTO 조립
        return ProjectDetailResponse.builder()
                .projectId(recruitment.getId())
                .isMine(isMine)
                .title(recruitment.getTitle())
                .teamStatus(recruitment.getTeamStatus())
                .bannerImageUrl(recruitment.getBannerImageUrl())
                .traits(recruitment.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .capacity(recruitment.getCapacity())
                .applicantCount(applicantCount)
                .mode(recruitment.getProjectMode())
                .address(recruitment.getRoadName() != null ? recruitment.getRoadName() : "ONLINE")
                .preferredAges(ProjectDetailResponse.PreferredAgesDto.builder()
                        .ageMin(recruitment.getAgeMin())
                        .ageMax(recruitment.getAgeMax())
                        .build())
                .expectedMonth(recruitment.getExpectedMonths())
                .startDate(recruitment.getStartDate())
                .detail(recruitment.getContentMd())
                .positions(positions)
                .leader(leader)
                .participants(participants)
                .build();
    }
}