package goorm.ddok.study.service;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.file.FileService;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.global.util.BannerImageService;
import goorm.ddok.member.domain.User;
import goorm.ddok.study.domain.*;
import goorm.ddok.study.dto.request.StudyRecruitmentCreateRequest;
import goorm.ddok.study.dto.response.StudyRecruitmentCreateResponse;
import goorm.ddok.study.repository.StudyApplicationRepository;
import goorm.ddok.study.repository.StudyParticipantRepository;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyRecruitmentService {

    private final StudyRecruitmentRepository studyRecruitmentRepository;
    private final StudyParticipantRepository participantRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    private final BannerImageService bannerImageService;
    private final FileService fileService;

    public StudyRecruitmentCreateResponse createStudy(StudyRecruitmentCreateRequest req,
                                                      MultipartFile bannerImage,
                                                      CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        User user = userDetails.getUser();

        if (req.getExpectedStart().isBefore(LocalDate.now())) {
            throw new GlobalException(ErrorCode.INVALID_START_DATE);
        }

        if (req.getMode() == StudyMode.offline) {
            LocationDto loc = req.getLocation();
            if (loc == null || loc.getLatitude() == null || loc.getLongitude() == null) {
                throw new GlobalException(ErrorCode.INVALID_LOCATION);
            }
        }

        // 연령대 범위 검증
        validatePreferredAges(req.getPreferredAges());

        Integer ageMin = (req.getPreferredAges() != null) ? req.getPreferredAges().getAgeMin() : null;
        Integer ageMax = (req.getPreferredAges() != null) ? req.getPreferredAges().getAgeMax() : null;


      // 배너 이미지 업로드 or 기본값
        String bannerUrl = (bannerImage != null && !bannerImage.isEmpty())
                ? uploadBannerImage(bannerImage)
                : bannerImageService.generateBannerImageUrl(req.getTitle(), "STUDY", 1200, 600);

        StudyRecruitment.StudyRecruitmentBuilder b = StudyRecruitment.builder()
                .user(user)
                .title(req.getTitle())
                .teamStatus(TeamStatus.RECRUITING)
                .startDate(req.getExpectedStart())
                .expectedMonths(req.getExpectedMonth())
                .mode(req.getMode())
                .capacity(req.getCapacity())
                .bannerImageUrl(bannerUrl)
                .studyType(req.getStudyType())
                .contentMd(req.getDetail())
                .ageMin(ageMin)
                .ageMax(ageMax);

        if (req.getMode() == StudyMode.offline && req.getLocation() != null) {
            LocationDto loc = req.getLocation();
            b.region1DepthName(loc.getRegion1depthName())
                    .region2DepthName(loc.getRegion2depthName())
                    .region3DepthName(loc.getRegion3depthName())
                    .roadName(loc.getRoadName())
                    .mainBuildingNo(loc.getMainBuildingNo())
                    .subBuildingNo(loc.getSubBuildingNo())
                    .zoneNo(loc.getZoneNo())
                    .latitude(loc.getLatitude())
                    .longitude(loc.getLongitude());
        } else {
            b.region1DepthName(null).region2DepthName(null).region3DepthName(null)
                    .roadName(null).mainBuildingNo(null).subBuildingNo(null).zoneNo(null)
                    .latitude(null).longitude(null);
        }

        StudyRecruitment study = b.build();

        if (req.getTraits() != null) {
            req.getTraits().stream()
                    .filter(s -> s != null && !s.trim().isBlank())
                    .map(String::trim)
                    .distinct()
                    .forEach(tr -> study.getTraits().add(
                            StudyRecruitmentTrait.builder()
                                    .studyRecruitment(study)
                                    .traitName(tr)
                                    .build()
                    ));
        }

        try {
            studyRecruitmentRepository.save(study);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.STUDY_SAVE_FAILED);
        }

        participantRepository.save(StudyParticipant.builder()
                .studyRecruitment(study)
                .user(user)
                .role(ParticipantRole.LEADER)
                .build());

        return StudyRecruitmentCreateResponse.builder()
                .studyId(study.getId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .teamStatus(study.getTeamStatus())
                .title(study.getTitle())
                .expectedStart(study.getStartDate())
                .expectedMonth(study.getExpectedMonths())
                .mode(study.getMode())
                .location((study.getLatitude() != null && study.getLongitude() != null)
                        ? LocationDto.builder()
                        .latitude(study.getLatitude())
                        .longitude(study.getLongitude())
                        .address(study.getRoadName())
                        .region1depthName(study.getRegion1DepthName())
                        .region2depthName(study.getRegion2DepthName())
                        .region3depthName(study.getRegion3DepthName())
                        .roadName(study.getRoadName())
                        .mainBuildingNo(study.getMainBuildingNo())
                        .subBuildingNo(study.getSubBuildingNo())
                        .zoneNo(study.getZoneNo())
                        .build() : null)
                .preferredAges(PreferredAgesDto.builder().ageMin(study.getAgeMin()).ageMax(study.getAgeMax()).build())
                .capacity(study.getCapacity())
                .bannerImageUrl(study.getBannerImageUrl())
                .traits(study.getTraits().stream().map(StudyRecruitmentTrait::getTraitName).toList())
                .studyType(study.getStudyType())
                .detail(study.getContentMd())
                .build();
    }

    private void validatePreferredAges(PreferredAgesDto preferredAges) {
        if (preferredAges == null) return; // 연령 무관

        Integer min = preferredAges.getAgeMin();
        Integer max = preferredAges.getAgeMax();

        if (min == null || max == null) {
            throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
        }
        if (min < 10 || max > 110) { // 100대까지
            throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
        }
        if (min >= max) {
            throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
        }
        if (min % 10 != 0 || max % 10 != 0) {
            throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
        }
    }

    private String uploadBannerImage(MultipartFile file) {
        try {
            return fileService.upload(file);
        } catch (IOException e) {
            throw new GlobalException(ErrorCode.BANNER_UPLOAD_FAILED);
        }
    }

    /** 신청 토글 */
    @Transactional
    public boolean toggleJoin(CustomUserDetails userDetails, Long studyId) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = userDetails.getUser().getId();

        StudyRecruitment study = studyRecruitmentRepository.findById(studyId)
                .orElseThrow(() -> new GlobalException(ErrorCode.STUDY_NOT_FOUND));
        if (study.getDeletedAt() != null) throw new GlobalException(ErrorCode.STUDY_NOT_FOUND);

        if (study.getUser().getId().equals(userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN_ACTION);
        }

        return studyApplicationRepository.findByUser_IdAndStudyRecruitment_Id(userId, studyId)
                .map(existing -> { studyApplicationRepository.delete(existing); return false; })
                .orElseGet(() -> {
                    studyApplicationRepository.save(StudyApplication.builder()
                            .user(userDetails.getUser())
                            .studyRecruitment(study)
                            .applicationStatus(ApplicationStatus.PENDING)
                            .build());
                    return true;
                });
    }
}