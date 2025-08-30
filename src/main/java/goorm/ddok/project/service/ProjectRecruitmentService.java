package goorm.ddok.project.service;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.file.FileService;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.global.util.BannerImageService;
import goorm.ddok.member.domain.User;
import goorm.ddok.project.domain.*;
import goorm.ddok.project.dto.request.ProjectRecruitmentCreateRequest;
import goorm.ddok.project.dto.response.ProjectRecruitmentResponse;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentPositionRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import lombok.RequiredArgsConstructor;
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

        // 1) 검증
        if (request.getMode() == ProjectMode.OFFLINE) {
            LocationDto loc = request.getLocation();
            if (loc == null || loc.getLatitude() == null || loc.getLongitude() == null) {
                throw new GlobalException(ErrorCode.INVALID_LOCATION);
            }
        }
        if (request.getPreferredAges() != null &&
                request.getPreferredAges().getAgeMin() > request.getPreferredAges().getAgeMax()) {
            throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
        }
        if (!request.getPositions().contains(request.getLeaderPosition())) {
            throw new GlobalException(ErrorCode.INVALID_LEADER_POSITION);
        }
        if (request.getExpectedStart().isBefore(LocalDate.now())) {
            throw new GlobalException(ErrorCode.INVALID_START_DATE);
        }

        // 2) 배너
        String bannerImageUrl = (bannerImage != null && !bannerImage.isEmpty())
                ? uploadBannerImage(bannerImage)
                : bannerImageService.generateBannerImageUrl(request.getTitle(), "PROJECT", 1200, 600);

        // 3) 엔티티 생성 (주소 필드 개별 저장)
        ProjectRecruitment.ProjectRecruitmentBuilder builder = ProjectRecruitment.builder()
                .user(user)
                .title(request.getTitle())
                .teamStatus(TeamStatus.RECRUITING)
                .startDate(request.getExpectedStart())
                .expectedMonths(request.getExpectedMonth())
                .projectMode(request.getMode())
                .ageMin(request.getPreferredAges() != null ? request.getPreferredAges().getAgeMin() : 0)
                .ageMax(request.getPreferredAges() != null ? request.getPreferredAges().getAgeMax() : 0)
                .capacity(request.getCapacity())
                .bannerImageUrl(bannerImageUrl)
                .contentMd(request.getDetail())
                .createdAt(Instant.now())
                .updatedAt(Instant.now());

        if (request.getMode() == ProjectMode.OFFLINE && request.getLocation() != null) {
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

        // 4) positions / traits
        List<ProjectRecruitmentPosition> positions = request.getPositions().stream()
                .filter(s -> s != null && !s.trim().isBlank())
                .map(String::trim)
                .distinct()
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

        // 5) 저장
        try {
            projectRecruitmentRepository.save(recruitment);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.PROJECT_SAVE_FAILED);
        }

        // 6) 리더 참가자 등록
        ProjectRecruitmentPosition leaderPos = recruitment.getPositions().stream()
                .filter(p -> p.getPositionName().equals(request.getLeaderPosition()))
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

        // 7) 응답 DTO: 주소는 합쳐서 내려주기
        LocationDto respLocation = null;
        if (recruitment.getProjectMode() == ProjectMode.OFFLINE) {
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

        return ProjectRecruitmentResponse.builder()
                .projectId(recruitment.getId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .leaderPosition(request.getLeaderPosition())
                .title(recruitment.getTitle())
                .teamStatus(recruitment.getTeamStatus())
                .expectedStart(recruitment.getStartDate())
                .expectedMonth(recruitment.getExpectedMonths())
                .mode(recruitment.getProjectMode())
                .location(respLocation) // ONLINE이면 null
                .preferredAges(PreferredAgesDto.builder()
                        .ageMin(recruitment.getAgeMin())
                        .ageMax(recruitment.getAgeMax())
                        .build())
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
        if (pr.getProjectMode() == ProjectMode.ONLINE) return "ONLINE";
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

    // toggleJoin 메서드는 변경 없음
    @Transactional
    public boolean toggleJoin(CustomUserDetails userDetails, Long projectId, String appliedPosition) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = userDetails.getUser().getId();

        ProjectRecruitment project = projectRecruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROJECT_NOT_FOUND));

        if (project.getUser().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN_ACTION);
        }

        Optional<ProjectApplication> existingApplication =
                projectApplicationRepository.findByUser_IdAndPosition_ProjectRecruitment_Id(userId, projectId);

        if (existingApplication.isPresent()) {
            ProjectApplication existing = existingApplication.get();
            if (appliedPosition == null
                    || existing.getPosition().getPositionName().equals(appliedPosition)) {
                projectApplicationRepository.delete(existing);
                return false;
            } else {
                throw new GlobalException(ErrorCode.ALREADY_APPLIED);
            }
        }

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
        return true;
    }
}