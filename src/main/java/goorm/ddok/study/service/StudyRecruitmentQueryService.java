package goorm.ddok.study.service;

import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.global.util.AddressNormalizer;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import goorm.ddok.reputation.domain.UserReputation;
import goorm.ddok.reputation.repository.UserReputationRepository;
import goorm.ddok.study.domain.*;
import goorm.ddok.study.dto.UserSummaryDto;
import goorm.ddok.study.dto.response.StudyRecruitmentDetailResponse;
import goorm.ddok.study.repository.StudyApplicationRepository;
import goorm.ddok.study.repository.StudyParticipantRepository;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRecruitmentQueryService {

    private final StudyRecruitmentRepository studyRecruitmentRepository;
    private final StudyParticipantRepository studyParticipantRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    private final UserReputationRepository userReputationRepository;

    /**
     * 스터디 모집글 상세 조회
     */
    public StudyRecruitmentDetailResponse getStudyDetail(
            Long studyId,
            CustomUserDetails userDetails
    ) {
        // 1. 스터디 모집글 존재 여부 확인
        StudyRecruitment study = studyRecruitmentRepository.findById(studyId)
                .orElseThrow(() -> new GlobalException(ErrorCode.RECRUITMENT_NOT_FOUND));

        User currentUser = (userDetails != null) ? userDetails.getUser() : null;

        // 2. 참여자 전체 조회 (리더 + 멤버)
        List<StudyParticipant> participants = studyParticipantRepository.findByStudyRecruitment(study);

        // 3. 리더 조회
        StudyParticipant leader = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.LEADER)
                .findFirst()
                .orElseThrow(() -> new GlobalException(ErrorCode.LEADER_NOT_FOUND));

        // 4. 지원자 수 (모집글 단위, 확정 포함)
        int applicantCount = (int) studyApplicationRepository.countByStudyRecruitment(study);

        // 5. 확정 멤버 수 (리더 제외)
        long memberCount = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.MEMBER)
                .count();

        // 6. 총 참여자 수 (리더 + 멤버)
        int participantsCount = (int) (memberCount + 1);

        // 7. 리더 DTO
        BigDecimal leaderTemp = userReputationRepository.findByUserId(leader.getUser().getId())
                .map(UserReputation::getTemperature)
                .orElse(BigDecimal.valueOf(36.5));
        // TODO: 온도 로직 완성시 수정
//                            .orElseThrow(() -> new GlobalException(ErrorCode.REPUTATION_NOT_FOUND));
        UserSummaryDto leaderDto = toUserSummaryDto(leader, currentUser, leaderTemp);

        // 8. 멤버 DTO 리스트 (리더 제외)
        List<UserSummaryDto> memberDtos = participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.MEMBER)
                .map(p -> {
                    BigDecimal temp = userReputationRepository.findByUserId(p.getUser().getId())
                            .map(UserReputation::getTemperature)
                            .orElse(BigDecimal.valueOf(36.5));
                    // TODO: 온도 로직 완성시 수정
//                            .orElseThrow(() -> new GlobalException(ErrorCode.REPUTATION_NOT_FOUND));
                    return toUserSummaryDto(p, currentUser, temp);
                })
                .toList();

        // 9. 응답 조립
        return StudyRecruitmentDetailResponse.builder()
                .studyId(study.getId())
                .title(study.getTitle())
                .studyType(study.getStudyType())
                .IsMine(currentUser != null && study.getUser().getId().equals(currentUser.getId()))
                .IsApplied(isApplied(currentUser, study))       // 지원 여부 (Application 기준)
                .IsApproved(isApproved(currentUser, participants)) // 승인 여부 (Participant 기준)
                .teamStatus(study.getTeamStatus())
                .bannerImageUrl(study.getBannerImageUrl())
                .traits(study.getTraits().stream().map(StudyRecruitmentTrait::getTraitName).toList())
                .capacity(study.getCapacity())
                .applicantCount(applicantCount)   // 지원자 수 (확정 포함)
                .participantsCount(participantsCount) // 총 참여자 수 (리더 + 멤버)
                .mode(study.getMode())
                .address(resolveAddress(study))
                .preferredAges(PreferredAgesDto.builder()
                        .ageMin(study.getAgeMin())
                        .ageMax(study.getAgeMax())
                        .build())
                .expectedMonth(study.getExpectedMonths())
                .startDate(study.getStartDate())
                .detail(study.getContentMd())
                .leader(leaderDto)
                .participants(memberDtos)
                .build();
    }

    /** 지원 여부 확인 (Application 기준) */
    private boolean isApplied(User currentUser, StudyRecruitment study) {
        if (currentUser == null) return false;
        return studyApplicationRepository.findByUser_IdAndStudyRecruitment_Id(
                currentUser.getId(), study.getId()
        ).isPresent();
    }

    /** 승인 여부 확인 (Participant 기준) */
    private boolean isApproved(User currentUser, List<StudyParticipant> participants) {
        if (currentUser == null) return false;
        return participants.stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUser.getId())
                        && p.getRole() == ParticipantRole.MEMBER);
    }

    /** 주소 변환 */
    private String resolveAddress(StudyRecruitment study) {
        if (study.getMode() == StudyMode.ONLINE) {
            return null;
        }
        if (study.getRegion1DepthName() != null && study.getRegion2DepthName() != null) {
            return AddressNormalizer.buildAddress(
                    study.getRegion1DepthName(),
                    study.getRegion2DepthName()
            );
        }
        throw new GlobalException(ErrorCode.INVALID_LOCATION);
    }

    /** UserSummaryDto 변환 */
    private UserSummaryDto toUserSummaryDto(StudyParticipant participant, User currentUser, BigDecimal temperature) {
        User user = participant.getUser();

        // 메인 포지션(PRIMARY)
        String mainPosition = user.getPositions().stream()
                .filter(pos -> pos.getType() == UserPositionType.PRIMARY)
                .map(UserPosition::getPositionName)
                .findFirst()
                .orElse(null);

        return UserSummaryDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .mainPosition(mainPosition)
                .temperature(temperature)
                .IsMine(currentUser != null && user.getId().equals(currentUser.getId()))
                .chatRoomId(null)         // TODO: 채팅 연동 시 교체
                .dmRequestPending(false)  // TODO: DM 연동 시 교체
                .build();
    }
}