package goorm.ddok.study.service;

import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.study.domain.*;
import goorm.ddok.study.dto.request.StudyRecruitmentCreateRequest;
import goorm.ddok.study.dto.response.StudyRecruitmentCreateResponse;
import goorm.ddok.study.repository.StudyParticipantRepository;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyRecruitmentService {

    private final StudyRecruitmentRepository studyRecruitmentRepository;
    private final StudyParticipantRepository participantRepository;
    private final StudyBannerImageService studyBannerImageService;
    private final FileSevice fileService;

    public StudyRecruitmentCreateResponse createStudy(
            StudyRecruitmentCreateRequest request,
            MultipartFile bannerImage,
            CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();

        // 1. 배너 이미지 업로드 or 기본값
        String bannerImageUrl;
        if (bannerImage != null && !bannerImage.isEmpty()) {
            try {
                bannerImageUrl = fileService.upload(bannerImage);
            } catch (IOException e) {
                throw new RuntimeException("배너 이미지 업로드 실패", e);
            }
        } else {
            bannerImageUrl = studyBannerImageService.generateBannerImageUrl(
                    request.getTitle(), 1200, 600
            );
        }

        // 2. StudyRecruitment 생성
        StudyRecruitment study = StudyRecruitment.builder()
                .user(user)
                .title(request.getTitle())
                .teamStatus(TeamStatus.RECRUITING)
                .startDate(request.getExpectedStart())
                .expectedMonths(request.getExpectedMonth())
                .mode(StudyMode.valueOf(request.getMode().toUpperCase()))
                .region1DepthName(resolveRegion1(request))
                .region2DepthName(resolveRegion2(request))
                .region3DepthName(resolveRegion3(request))
                .roadName(resolveRoadName(request))
                .latitude(request.getLocation() != null ? request.getLocation().getLatitude() : null)
                .longitude(request.getLocation() != null ? request.getLocation().getLongitude() : null)
                .ageMin(request.getPreferredAges() != null ? request.getPreferredAges().getAgeMin() : null)
                .ageMax(request.getPreferredAges() != null ? request.getPreferredAges().getAgeMax() : null)
                .capacity(request.getCapacity())
                .bannerImageUrl(bannerImageUrl)
                .studyType(StudyType.valueOf(request.getStudyType().toUpperCase()))
                .contentMd(request.getDetail())
                .build();

        // 3. traits 매핑
        if (request.getTraits() != null) {
            request.getTraits().forEach(tr ->
                    study.getTraits().add(
                            StudyRecruitmentTrait.builder()
                                    .studyRecruitment(study)
                                    .traitName(tr)
                                    .build()
                    )
            );
        }

        // 4. 저장
        studyRecruitmentRepository.save(study);

        // 5. 리더 Participant 등록
        StudyParticipant leader = StudyParticipant.builder()
                .studyRecruitment(study)
                .user(user)
                .role(ParticipantRole.LEADER)
                .build();
        participantRepository.save(leader);

        // 6. 응답 DTO 변환
        return StudyRecruitmentCreateResponse.builder()
                .studyId(study.getId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .teamStatus(study.getTeamStatus().name())
                .title(study.getTitle())
                .expectedStart(study.getStartDate())
                .expectedMonth(study.getExpectedMonths())
                .mode(study.getMode().name())
                .location(StudyRecruitmentCreateResponse.LocationDto.builder()
                        .latitude(study.getLatitude())
                        .longitude(study.getLongitude())
                        .address(study.getRoadName())
                        .build())
                .preferredAges(StudyRecruitmentCreateResponse.PreferredAgesDto.builder()
                        .ageMin(study.getAgeMin())
                        .ageMax(study.getAgeMax())
                        .build())
                .capacity(study.getCapacity())
                .bannerImageUrl(study.getBannerImageUrl())
                .traits(study.getTraits().stream().map(StudyRecruitmentTrait::getTraitName).toList())
                .studyType(study.getStudyType().name())
                .detail(study.getContentMd())
                .build();
    }

    // TODO: 실제 location 파싱 로직
    private String resolveRegion1(StudyRecruitmentCreateRequest request) {
        return request.getLocation() != null ? "서울특별시" : null;
    }
    private String resolveRegion2(StudyRecruitmentCreateRequest request) {
        return request.getLocation() != null ? "강남구" : null;
    }
    private String resolveRegion3(StudyRecruitmentCreateRequest request) {
        return request.getLocation() != null ? "역삼동" : null;
    }
    private String resolveRoadName(StudyRecruitmentCreateRequest request) {
        return request.getLocation() != null ? request.getLocation().getAddress() : null;
    }
}
