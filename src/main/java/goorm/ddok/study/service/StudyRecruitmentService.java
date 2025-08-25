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
import goorm.ddok.study.repository.StudyParticipantRepository;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyRecruitmentService {

    private final StudyRecruitmentRepository studyRecruitmentRepository;
    private final StudyParticipantRepository participantRepository;
    private final BannerImageService bannerImageService;
    private final FileService fileService;

    public StudyRecruitmentCreateResponse createStudy(
            StudyRecruitmentCreateRequest request,
            MultipartFile bannerImage,
            CustomUserDetails userDetails
    ) {
        // 로그인 유저 검증
        if (userDetails == null || userDetails.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        User user = userDetails.getUser();

        // 시작일 검증 (과거 날짜 금지)
        if (request.getExpectedStart().isBefore(LocalDate.now())) {
            throw new GlobalException(ErrorCode.INVALID_START_DATE);
        }

        // 오프라인 모드인데 위치가 없거나 위/경도가 없는 경우
        if (request.getMode() == StudyMode.OFFLINE) {
            if (request.getLocation() == null
                    || request.getLocation().getLatitude() == null
                    || request.getLocation().getLongitude() == null) {
                throw new GlobalException(ErrorCode.INVALID_LOCATION);
            }
        }

        // 연령대 범위 검증
        if (request.getPreferredAges().getAgeMin() > request.getPreferredAges().getAgeMax()) {
            throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
        }


        // 1. 배너 이미지 업로드 or 기본값
        String bannerImageUrl = (bannerImage != null && !bannerImage.isEmpty())
                ? uploadBannerImage(bannerImage)
                :bannerImageService.generateBannerImageUrl(request.getTitle(), "STUDY", 1200, 600);

        // 2. StudyRecruitment 생성
        StudyRecruitment study = StudyRecruitment.builder()
                .user(user)
                .title(request.getTitle())
                .teamStatus(TeamStatus.RECRUITING)
                .startDate(request.getExpectedStart())
                .expectedMonths(request.getExpectedMonth())
                .mode(request.getMode())
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
                .studyType(request.getStudyType())
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
        try {
            studyRecruitmentRepository.save(study);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.STUDY_SAVE_FAILED);
        }

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
                .teamStatus(study.getTeamStatus())
                .title(study.getTitle())
                .expectedStart(study.getStartDate())
                .expectedMonth(study.getExpectedMonths())
                .mode(study.getMode())
                .location(study.getLatitude() != null && study.getLongitude() != null
                        ? LocationDto.builder()
                        .latitude(study.getLatitude())
                        .longitude(study.getLongitude())
                        .address(study.getRoadName())
                        .build()
                        : null)
                .preferredAges(PreferredAgesDto.builder()
                        .ageMin(study.getAgeMin())
                        .ageMax(study.getAgeMax())
                        .build())
                .capacity(study.getCapacity())
                .bannerImageUrl(study.getBannerImageUrl())
                .traits(study.getTraits().stream().map(StudyRecruitmentTrait::getTraitName).toList())
                .studyType(study.getStudyType())
                .detail(study.getContentMd())
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
