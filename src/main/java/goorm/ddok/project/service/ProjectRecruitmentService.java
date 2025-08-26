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
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectRecruitmentService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final ProjectParticipantRepository participantRepository;
    private final BannerImageService bannerImageService;
    private final FileService fileService;

    public ProjectRecruitmentResponse createProject(
            ProjectRecruitmentCreateRequest request,
            MultipartFile bannerImage,
            CustomUserDetails userDetails
    ) {
        // 로그인 유저 검증
        if (userDetails == null || userDetails.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        User user = userDetails.getUser();

        // 오프라인 모드인데 위치가 없거나 위/경도가 없는 경우
        if (request.getMode() == ProjectMode.OFFLINE) {
            if (request.getLocation() == null
                    || request.getLocation().getLatitude() == null
                    || request.getLocation().getLongitude() == null) {
                throw new GlobalException(ErrorCode.INVALID_LOCATION);
            }
        }

        // 연령대 범위 검증
        if (request.getPreferredAges() != null &&
                request.getPreferredAges().getAgeMin() > request.getPreferredAges().getAgeMax()) {
            throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
        }

        // 리더 포지션이 모집 포지션에 없는 경우
        if (!request.getPositions().contains(request.getLeaderPosition())) {
            throw new GlobalException(ErrorCode.INVALID_LEADER_POSITION);
        }

        // 예상 시작일 검증 (과거 날짜 금지)
        if (request.getExpectedStart().isBefore(LocalDate.now())) {
            throw new GlobalException(ErrorCode.INVALID_START_DATE);
        }

        // 1. 배너 이미지 업로드 or 기본값
        String bannerImageUrl = (bannerImage != null && !bannerImage.isEmpty())
                ? uploadBannerImage(bannerImage)
                : bannerImageService.generateBannerImageUrl(request.getTitle(), "PROJECT", 1200, 600);

        // 2. ProjectRecruitment 생성
        ProjectRecruitment recruitment = ProjectRecruitment.builder()
                .user(user)
                .title(request.getTitle())
                .teamStatus(TeamStatus.RECRUITING)
                .startDate(request.getExpectedStart())
                .expectedMonths(request.getExpectedMonth())
                .projectMode(request.getMode())
                .region1depthName(resolveRegion1(request))
                .region2depthName(resolveRegion2(request))
                .region3depthName(resolveRegion3(request))
                .roadName(resolveRoadName(request))
                .latitude(request.getLocation() != null ? request.getLocation().getLatitude() : null)
                .longitude(request.getLocation() != null ? request.getLocation().getLongitude() : null)
                .ageMin(request.getPreferredAges() != null ? request.getPreferredAges().getAgeMin() : 0)
                .ageMax(request.getPreferredAges() != null ? request.getPreferredAges().getAgeMax() : 0)
                .capacity(request.getCapacity())
                .bannerImageUrl(bannerImageUrl)
                .contentMd(request.getDetail())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // 3. positions & traits 매핑
        List<ProjectRecruitmentPosition> positions = request.getPositions().stream()
                .map(pos -> ProjectRecruitmentPosition.builder()
                        .projectRecruitment(recruitment)
                        .positionName(pos)
                        .build())
                .toList();

        List<ProjectRecruitmentTrait> traits = request.getTraits() != null
                ? request.getTraits().stream()
                .map(tr -> ProjectRecruitmentTrait.builder()
                        .projectRecruitment(recruitment)
                        .traitName(tr)
                        .build())
                .toList()
                : List.of();

        recruitment.getPositions().addAll(positions);
        recruitment.getTraits().addAll(traits);

        // 4. 저장
        try {
            projectRecruitmentRepository.save(recruitment);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.PROJECT_SAVE_FAILED);
        }

        // 5. 리더 Participant 등록
        ProjectRecruitmentPosition leaderPos = positions.stream()
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

        // 6. 응답 DTO 변환
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
                .location(recruitment.getLatitude() != null && recruitment.getLongitude() != null
                        ? LocationDto.builder()
                        .latitude(recruitment.getLatitude())
                        .longitude(recruitment.getLongitude())
                        .address(recruitment.getRoadName())
                        .build()
                        : null)
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

    // TODO: 실제 location 파싱 로직
    private String resolveRegion1(ProjectRecruitmentCreateRequest request) {
        return request.getLocation() != null ? "서울특별시" : null;
    }
    private String resolveRegion2(ProjectRecruitmentCreateRequest request) {
        return request.getLocation() != null ? "강남구" : null;
    }
    private String resolveRegion3(ProjectRecruitmentCreateRequest request) {
        return request.getLocation() != null ? "테헤란로" : null;
    }
    private String resolveRoadName(ProjectRecruitmentCreateRequest request) {
        return request.getLocation() != null ? request.getLocation().getAddress() : null;
    }
}
