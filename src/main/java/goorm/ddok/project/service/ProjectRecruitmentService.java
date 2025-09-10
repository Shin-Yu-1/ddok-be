package goorm.ddok.project.service;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.file.FileService;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.global.util.BannerImageService;
import goorm.ddok.member.domain.User;
import goorm.ddok.notification.event.ProjectJoinRequestDecisionEvent;
import goorm.ddok.notification.event.ProjectJoinRequestedEvent;
import goorm.ddok.project.domain.*;
import goorm.ddok.project.dto.request.ProjectRecruitmentCreateRequest;
import goorm.ddok.project.dto.response.ProjectRecruitmentResponse;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentPositionRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import goorm.ddok.team.domain.TeamType;
import goorm.ddok.team.service.TeamCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectRecruitmentService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final ProjectParticipantRepository participantRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectRecruitmentPositionRepository projectRecruitmentPositionRepository;
    private final BannerImageService bannerImageService;
    private final FileService fileService;
    private final TeamCommandService teamCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public ProjectRecruitmentResponse createProject(
            ProjectRecruitmentCreateRequest request,
            MultipartFile bannerImage,
            CustomUserDetails userDetails
    ) {
        // 0) 인증
        if (userDetails == null || userDetails.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        User user = userDetails.getUser();

        // 1) 포지션 정규화 (트리밍 + 중복 제거)
        List<String> desiredPositions = request.getPositions().stream()
                .filter(s -> s != null && !s.trim().isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        String leaderPosName = request.getLeaderPosition() == null ? null : request.getLeaderPosition().trim();

        if (desiredPositions.isEmpty()) {
            throw new GlobalException(ErrorCode.INVALID_POSITIONS);
        }
        if (leaderPosName == null || leaderPosName.isBlank() || !desiredPositions.contains(leaderPosName)) {
            throw new GlobalException(ErrorCode.INVALID_LEADER_POSITION);
        }

        // (선택) capacity <= unique positions 강제 — 생성 단계도 편의상 동일 정책 적용
        if (request.getCapacity() != null && request.getCapacity() < desiredPositions.size()) {
            throw new GlobalException(ErrorCode.INVALID_CAPACITY_POSITIONS);
        }

        // 2) 위치 검증
        if (request.getMode() == ProjectMode.offline) {
            LocationDto loc = request.getLocation();
            if (loc == null || loc.getLatitude() == null || loc.getLongitude() == null) {
                throw new GlobalException(ErrorCode.INVALID_LOCATION);
            }
        }

        // 3) 연령대 검증 (null=무관, 값이 있으면 범위+10단위 강제)
        int ageMin, ageMax;
        if (request.getPreferredAges() == null) {
            ageMin = 0;
            ageMax = 0;
        } else {
            ageMin = request.getPreferredAges().getAgeMin();
            ageMax = request.getPreferredAges().getAgeMax();
            if (ageMin > ageMax) {
                throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
            }
            // ★ 10단위 강제
            if (ageMin % 10 != 0 || ageMax % 10 != 0) {
                throw new GlobalException(ErrorCode.INVALID_AGE_BUCKET);
            }
        }

        // 4) 시작일 검증
        if (request.getExpectedStart().isBefore(LocalDate.now())) {
            throw new GlobalException(ErrorCode.INVALID_START_DATE);
        }

        // 5) 배너
        String bannerImageUrl = (bannerImage != null && !bannerImage.isEmpty())
                ? uploadBannerImage(bannerImage)
                : bannerImageService.generateBannerImageUrl(request.getTitle(), "PROJECT", 1200, 600);

        // 6) 엔티티 생성 (주소 필드 개별 저장)
        ProjectRecruitment.ProjectRecruitmentBuilder builder = ProjectRecruitment.builder()
                .user(user)
                .title(request.getTitle())
                .teamStatus(TeamStatus.RECRUITING)
                .startDate(request.getExpectedStart())
                .expectedMonths(request.getExpectedMonth())
                .projectMode(request.getMode())
                .ageMin(ageMin)
                .ageMax(ageMax)
                .capacity(request.getCapacity())
                .bannerImageUrl(bannerImageUrl)
                .contentMd(request.getDetail())
                .createdAt(Instant.now())
                .updatedAt(Instant.now());

        if (request.getMode() == ProjectMode.offline && request.getLocation() != null) {
            LocationDto loc = request.getLocation();
            builder
                    .region1depthName(loc.getRegion1depthName())
                    .region2depthName(loc.getRegion2depthName())
                    .region3depthName(loc.getRegion3depthName())
                    .roadName(loc.getRoadName())
                    .mainBuildingNo(loc.getMainBuildingNo())
                    .subBuildingNo(loc.getSubBuildingNo())
                    .zoneNo(loc.getZoneNo())
                    .latitude(loc.getLatitude())
                    .longitude(loc.getLongitude());
        } else {
            builder
                    .region1depthName(null)
                    .region2depthName(null)
                    .region3depthName(null)
                    .roadName(null)
                    .mainBuildingNo(null)
                    .subBuildingNo(null)
                    .zoneNo(null)
                    .latitude(null)
                    .longitude(null);
        }

        ProjectRecruitment recruitment = builder.build();

        // 7) positions / traits
        List<ProjectRecruitmentPosition> positions = desiredPositions.stream()
                .map(pos -> ProjectRecruitmentPosition.builder()
                        .projectRecruitment(recruitment)
                        .positionName(pos)
                        .build())
                .toList();

        List<ProjectRecruitmentTrait> traits = (request.getTraits() == null ? List.<String>of() : request.getTraits())
                .stream()
                .filter(s -> s != null && !s.trim().isBlank())
                .map(String::trim)
                .distinct()
                .map(tr -> ProjectRecruitmentTrait.builder()
                        .projectRecruitment(recruitment)
                        .traitName(tr)
                        .build())
                .toList();

        recruitment.getPositions().addAll(positions);
        recruitment.getTraits().addAll(traits);

        // 8) 저장
        try {
            projectRecruitmentRepository.save(recruitment);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.PROJECT_SAVE_FAILED);
        }

        /**
         * 9) 팀 자동 생성
         * - 모집글이 생성되면 동시에 팀 엔티티를 생성한다.
         * - type: PROJECT (스터디일 경우 STUDY)
         * - title: 모집글 제목
         * - leader: 모집글 작성자
         * - 생성과 동시에 리더를 TeamMember(LEADER)로 자동 추가한다.
         */
        teamCommandService.createTeamForRecruitment(
                recruitment.getId(),
                TeamType.PROJECT,
                recruitment.getTitle(),
                user
        );

        // 10) 리더 참가자 등록 (정규화된 이름으로 검색)
        ProjectRecruitmentPosition leaderPos = recruitment.getPositions().stream()
                .filter(p -> p.getPositionName().equals(leaderPosName))
                .findFirst()
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_LEADER_POSITION));

        ProjectParticipant leader = ProjectParticipant.builder()
                .user(user)
                .position(leaderPos)
                .role(ParticipantRole.LEADER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        participantRepository.save(leader);

        // 10) 응답 DTO: 주소는 합쳐서 내려주기(offline일 때만)
        LocationDto respLocation = null;
        if (recruitment.getProjectMode() == ProjectMode.offline) {
            String fullAddress = composeAddress(recruitment); // "전북 익산시 부송동 망산길 11-17"
            respLocation = LocationDto.builder()
                    .address(fullAddress)
                    .region1depthName(recruitment.getRegion1depthName())
                    .region2depthName(recruitment.getRegion2depthName())
                    .region3depthName(recruitment.getRegion3depthName())
                    .roadName(recruitment.getRoadName())
                    .mainBuildingNo(recruitment.getMainBuildingNo())
                    .subBuildingNo(recruitment.getSubBuildingNo())
                    .zoneNo(recruitment.getZoneNo())
                    .latitude(recruitment.getLatitude())
                    .longitude(recruitment.getLongitude())
                    .build();
        }

        // preferredAges: 0/0(무관) → null 로 응답
        PreferredAgesDto respAges = (recruitment.getAgeMin() == 0 && recruitment.getAgeMax() == 0)
                ? null
                : PreferredAgesDto.builder()
                .ageMin(recruitment.getAgeMin())
                .ageMax(recruitment.getAgeMax())
                .build();

        return ProjectRecruitmentResponse.builder()
                .projectId(recruitment.getId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .leaderPosition(leaderPosName)
                .title(recruitment.getTitle())
                .teamStatus(recruitment.getTeamStatus())
                .expectedStart(recruitment.getStartDate())
                .expectedMonth(recruitment.getExpectedMonths())
                .mode(recruitment.getProjectMode())
                .location(respLocation) // online이면 null
                .preferredAges(respAges)
                .capacity(recruitment.getCapacity())
                .bannerImageUrl(recruitment.getBannerImageUrl())
                .traits(recruitment.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .positions(recruitment.getPositions().stream().map(ProjectRecruitmentPosition::getPositionName).toList())
                .detail(recruitment.getContentMd())
                .build();
    }

    private String uploadBannerImage(MultipartFile bannerImage) {
        try {
            return fileService.upload(bannerImage);
        } catch (IOException e) {
            throw new GlobalException(ErrorCode.BANNER_UPLOAD_FAILED);
        }
    }

    /** 엔티티에 저장된 주소 조합 -> "r1 r2 r3 road main-sub" */
    private String composeAddress(ProjectRecruitment pr) {
        if (pr.getProjectMode() == ProjectMode.online) return "online";
        String r1 = Optional.ofNullable(pr.getRegion1depthName()).orElse("");
        String r2 = Optional.ofNullable(pr.getRegion2depthName()).orElse("");
        String r3 = Optional.ofNullable(pr.getRegion3depthName()).orElse("");
        String road = Optional.ofNullable(pr.getRoadName()).orElse("");
        String main = Optional.ofNullable(pr.getMainBuildingNo()).orElse("");
        String sub  = Optional.ofNullable(pr.getSubBuildingNo()).orElse("");

        StringBuilder sb = new StringBuilder();
        if (!r1.isBlank()) sb.append(r1).append(" ");
        if (!r2.isBlank()) sb.append(r2).append(" ");
        if (!r3.isBlank()) sb.append(r3).append(" ");
        if (!road.isBlank()) sb.append(road).append(" ");
        if (!main.isBlank() && !sub.isBlank()) sb.append(main).append("-").append(sub);
        else if (!main.isBlank()) sb.append(main);

        String s = sb.toString().trim().replaceAll("\\s+", " ");
        return s.isBlank() ? "-" : s;
    }


    @Transactional
    public boolean toggleJoin(CustomUserDetails userDetails, Long projectId, String appliedPosition) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = userDetails.getUser().getId();
        String nickname = userDetails.getUser().getNickname(); // 닉네임 필드명에 맞게

        ProjectRecruitment project = projectRecruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROJECT_NOT_FOUND));

        if (project.getUser().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN_ACTION);
        }

        Optional<ProjectApplication> existingOpt =
                projectApplicationRepository.findByUser_IdAndPosition_ProjectRecruitment_Id(userId, projectId);

        if (existingOpt.isPresent()) {
            ProjectApplication existing = existingOpt.get();

            switch (existing.getStatus()) {
                case PENDING -> {
                    // ✅ 취소: 알림 발송 안 함
                    if (appliedPosition == null
                            || existing.getPosition().getPositionName().equals(appliedPosition)) {

                        int deleted = projectApplicationRepository.deleteByIdAndStatus(
                                existing.getId(), ApplicationStatus.PENDING);

                        if (deleted == 0) {
                            ApplicationStatus cur = projectApplicationRepository
                                    .findStatusById(existing.getId())
                                    .orElse(null);

                            if (cur == ApplicationStatus.APPROVED) {
                                throw new GlobalException(ErrorCode.APPLICATION_ALREADY_APPROVED);
                            }
                        }
                        return false; // isApplied = false
                    } else {
                        throw new GlobalException(ErrorCode.ALREADY_APPLIED);
                    }
                }
                case APPROVED -> throw new GlobalException(ErrorCode.APPLICATION_ALREADY_APPROVED);
                case REJECTED -> {
                    // ✅ REJECTED → PENDING (재신청) → 알림 발송 대상
                    String targetName = (appliedPosition == null || appliedPosition.isBlank())
                            ? existing.getPosition().getPositionName()
                            : appliedPosition;

                    ProjectRecruitmentPosition targetPos = projectRecruitmentPositionRepository
                            .findByProjectRecruitmentIdAndPositionName(projectId, targetName)
                            .orElseThrow(() -> new GlobalException(ErrorCode.POSITION_NOT_FOUND));

                    existing.changePosition(targetPos);
                    existing.setStatus(ApplicationStatus.PENDING);
                    projectApplicationRepository.save(existing);

                    // 🔔 이벤트 발행 (AFTER_COMMIT에서 푸시)
                    eventPublisher.publishEvent(
                            ProjectJoinRequestedEvent.builder()
                                    .applicationId(existing.getId())
                                    .applicantUserId(userId)
                                    .applicantNickname(nickname)
                                    .projectId(project.getId())
                                    .projectTitle(project.getTitle()) // 필드명에 맞게
                                    .appliedPosition(targetPos.getPositionName())
                                    .ownerUserId(project.getUser().getId())
                                    .build()
                    );

                    return true; // isApplied = true
                }
            }
            return false;
        }

        // 신규 신청 생성 → 알림 발송
        if (appliedPosition == null || appliedPosition.isBlank()) {
            throw new GlobalException(ErrorCode.POSITION_REQUIRED);
        }
        ProjectRecruitmentPosition position = projectRecruitmentPositionRepository
                .findByProjectRecruitmentIdAndPositionName(projectId, appliedPosition)
                .orElseThrow(() -> new GlobalException(ErrorCode.POSITION_NOT_FOUND));

        ProjectApplication newApp = ProjectApplication.builder()
                .user(userDetails.getUser())
                .position(position)
                .status(ApplicationStatus.PENDING)
                .build();
        projectApplicationRepository.save(newApp);

        eventPublisher.publishEvent(
                ProjectJoinRequestedEvent.builder()
                        .applicationId(newApp.getId())
                        .applicantUserId(userId)
                        .applicantNickname(nickname)
                        .projectId(project.getId())
                        .projectTitle(project.getTitle())
                        .appliedPosition(position.getPositionName())
                        .ownerUserId(project.getUser().getId())
                        .build()
        );


        return true;
    }
}