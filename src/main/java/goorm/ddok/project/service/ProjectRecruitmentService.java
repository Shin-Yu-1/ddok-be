package goorm.ddok.project.service;

import goorm.ddok.global.file.FileService;
import goorm.ddok.global.security.auth.CustomUserDetails;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectRecruitmentService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final ProjectParticipantRepository participantRepository;
    private final ProjectBannerImageService projectBannerImageService;
    private final FileService fileService;

    public ProjectRecruitmentResponse createProject(
            ProjectRecruitmentCreateRequest request,
            MultipartFile bannerImage,
            CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser(); // 로그인 유저 엔티티

        // 1. 배너 이미지 업로드 기본값 처리
        String bannerImageUrl;

        if (bannerImage != null && !bannerImage.isEmpty()) {
            try {
                bannerImageUrl = fileService.upload(bannerImage);
            } catch (IOException e) {
                throw new RuntimeException("배너 이미지 업로드 실패", e);
            }
        } else {
            bannerImageUrl = projectBannerImageService.generateBannerImageUrl(
                    request.getTitle(), 1200, 600
            );
        }

        // 2. ProjectRecruitment 생성
        ProjectRecruitment recruitment = ProjectRecruitment.builder()
                .user(user)
                .title(request.getTitle())
                .teamStatus(TeamStatus.RECRUTING)
                .startDate(request.getExpectedStart())
                .expectedMonths(request.getExpectedMonth())
                .projectMode(request.getMode())
                .region1depthName(resolveRegion1(request)) // TODO:location 파싱해서 넣기
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
                .collect(Collectors.toList());

        List<ProjectRecruitmentTrait> traits = request.getTraits() != null
                ? request.getTraits().stream()
                .map(tr -> ProjectRecruitmentTrait.builder()
                        .projectRecruitment(recruitment)
                        .traitName(tr)
                        .build())
                .collect(Collectors.toList())
                : List.of();

        recruitment.getPositions().addAll(positions);
        recruitment.getTraits().addAll(traits);

        // 4. 저장
        projectRecruitmentRepository.save(recruitment);

        // 5. 리더 Participant 등록
        //    leaderPosition 이름 찾아서 매핑
        ProjectRecruitmentPosition leaderPos = positions.stream()
                .filter(p -> p.getPositionName().equals(request.getLeaderPosition()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("리더 포지션이 모집 포지션에 존재하지 않습니다."));

        ProjectParticipant leader = ProjectParticipant.builder()
                .projectRecruitment(recruitment)
                .user(user)
                .position(leaderPos)
                .role(ParticipantRole.LEADER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        participantRepository.save(leader);

        // 6. 응답 DTO 변환
        return ProjectRecruitmentResponse.fromEntity(recruitment, user, request.getLeaderPosition());
    }

    // TODO: 실제 파싱 로직
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