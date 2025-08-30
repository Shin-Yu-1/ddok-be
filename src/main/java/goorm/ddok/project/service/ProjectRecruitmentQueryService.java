package goorm.ddok.project.service;

import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import goorm.ddok.reputation.repository.UserReputationRepository;
import goorm.ddok.project.domain.*;
import goorm.ddok.project.dto.ProjectPositionDto;
import goorm.ddok.project.dto.ProjectUserSummaryDto;
import goorm.ddok.project.dto.response.ProjectDetailResponse;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import goorm.ddok.reputation.domain.UserReputation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectRecruitmentQueryService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final UserReputationRepository userReputationRepository;
    private final ProjectApplicationRepository projectApplicationRepository;

    /** 프로젝트 모집글 상세 조회 */
    public ProjectDetailResponse getProjectDetail(Long projectId, CustomUserDetails userDetails) {
        // 1) 프로젝트 존재 확인
        ProjectRecruitment project = projectRecruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.RECRUITMENT_NOT_FOUND));

        User me = (userDetails != null) ? userDetails.getUser() : null;

        // 2) 참가자 전체 조회 (리더 포함)
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

                    boolean isApplied = (me != null) &&
                            projectApplicationRepository.findByUser_IdAndPosition_ProjectRecruitment_Id(
                                    me.getId(), project.getId()).isPresent();

                    boolean isApproved = (me != null) &&
                            participants.stream().anyMatch(p ->
                                    p.getRole() == ParticipantRole.MEMBER
                                            && p.getUser().getId().equals(me.getId())
                                            && Objects.equals(p.getPosition(), position));

                    boolean isAvailable = confirmedCount < project.getCapacity();

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

        // 6) 리더 요약 정보
        BigDecimal leaderTemp = userReputationRepository.findByUserId(leader.getUser().getId())
                .map(UserReputation::getTemperature)
                .orElse(BigDecimal.valueOf(36.5));
        ProjectUserSummaryDto leaderDto = toUserSummaryDto(leader, me, leaderTemp);

        // 7) 멤버 요약 정보(리더 제외)
        List<ProjectUserSummaryDto> memberDtos = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.MEMBER)
                .map(p -> {
                    BigDecimal temp = userReputationRepository.findByUserId(p.getUser().getId())
                            .map(UserReputation::getTemperature)
                            .orElse(BigDecimal.valueOf(36.5));
                    return toUserSummaryDto(p, me, temp);
                })
                .toList();

        // 8) 주소 조립
        String address = resolveFullAddress(project);

        // 9) 선호 연령: 0/0이면 null
        PreferredAgesDto ages = (project.getAgeMin() == 0 && project.getAgeMax() == 0)
                ? null
                : PreferredAgesDto.builder().ageMin(project.getAgeMin()).ageMax(project.getAgeMax()).build();

        // 10) 응답
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
                .address(address) // ← 합쳐진 전체 주소 또는 ONLINE/null
                .preferredAges(ages)
                .expectedMonth(project.getExpectedMonths())
                .startDate(project.getStartDate())
                .detail(project.getContentMd())
                .positions(positionDtos)
                .leader(leaderDto)
                .participants(memberDtos)
                .build();
    }

    /** 전체 주소 조립(ONLINE 이면 null).
     *  저장 정책:
     *  - OFFLINE 저장 시, 프론트가 Kakao road_address로 보낸 값을
     *    region1/2/3 + roadName + mainBuildingNo/subBuildingNo(있다면) 로 합쳐 roadName 필드에 풀 주소를 저장한 경우가 많음.
     *  - 우선 순위: roadName(풀주소로 저장돼 있으면 그대로) > 조합(region1/2/3 + roadName + 빌딩번호)
     */
    private String resolveFullAddress(ProjectRecruitment pr) {
        if (pr.getProjectMode() == ProjectMode.ONLINE) {
            return null;
        }

        // roadName 자체가 이미 완성형(예: "전북 익산시 부송동 망산길 11-17")으로 저장돼 있을 수 있음
        if (pr.getRoadName() != null && !pr.getRoadName().isBlank()) {
            return pr.getRoadName().trim();
        }

        // 도메인에 main/sub 빌딩 번호 컬럼이 없다면(현재 구조) region + roadName만 조합
        String r1 = Optional.ofNullable(pr.getRegion1depthName()).orElse("");
        String r2 = Optional.ofNullable(pr.getRegion2depthName()).orElse("");
        String r3 = Optional.ofNullable(pr.getRegion3depthName()).orElse("");
        String road = Optional.ofNullable(pr.getRoadName()).orElse("");

        String base = String.join(" ", new String[]{
                r1, r2, r3, road
        }).replaceAll("\\s+", " ").trim();

        return base.isBlank() ? null : base;
    }

    /** UserSummary 변환 */
    private ProjectUserSummaryDto toUserSummaryDto(ProjectParticipant participant, User currentUser, BigDecimal temperature) {
        User user = participant.getUser();

        String mainPosition = user.getPositions().stream()
                .filter(pos -> pos.getType() == UserPositionType.PRIMARY)
                .map(UserPosition::getPositionName)
                .findFirst()
                .orElse(null);

        return ProjectUserSummaryDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .mainPosition(mainPosition)
                .mainBadge(null)       // TODO: 뱃지 매핑 시 연결
                .abandonBadge(null)    // TODO: 이탈 뱃지 매핑 시 연결
                .temperature(temperature)
                .decidedPosition(participant.getPosition().getPositionName())
                .IsMine(currentUser != null && user.getId().equals(currentUser.getId()))
                .chatRoomId(null)
                .dmRequestPending(false)
                .build();
    }
}