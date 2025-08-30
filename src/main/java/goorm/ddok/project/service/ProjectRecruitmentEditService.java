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
import goorm.ddok.project.dto.request.ProjectRecruitmentUpdateRequest;
import goorm.ddok.project.dto.response.ProjectEditPageResponse;
import goorm.ddok.project.dto.response.ProjectUpdateResultResponse;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectRecruitmentEditService {

    private final ProjectRecruitmentRepository recruitmentRepository;
    private final ProjectParticipantRepository participantRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final FileService fileService;
    private final BannerImageService bannerImageService;

    /* =========================
     *  수정 페이지 조회
     * ========================= */
    @Transactional(readOnly = true)
    public ProjectEditPageResponse getEditPage(Long projectId, CustomUserDetails me) {
        if (me == null || me.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);

        ProjectRecruitment pr = recruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROJECT_NOT_FOUND));

        // 리더만 조회
        if (!Objects.equals(pr.getUser().getId(), me.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        long applicantCount = applicationRepository.countAllByProjectId(projectId);

        Map<String, Long> applied = applicationRepository.countAppliedByPosition(projectId)
                .stream().collect(Collectors.toMap(
                        ProjectApplicationRepository.PositionCountProjection::getPositionName,
                        ProjectApplicationRepository.PositionCountProjection::getCnt
                ));

        Map<String, Long> confirmed = applicationRepository.countApprovedByPosition(projectId)
                .stream().collect(Collectors.toMap(
                        ProjectApplicationRepository.PositionCountProjection::getPositionName,
                        ProjectApplicationRepository.PositionCountProjection::getCnt
                ));

        List<ProjectEditPageResponse.PositionItem> positions = pr.getPositions().stream()
                .map(p -> new ProjectEditPageResponse.PositionItem(
                        p.getPositionName(),
                        applied.getOrDefault(p.getPositionName(), 0L),
                        confirmed.getOrDefault(p.getPositionName(), 0L)
                ))
                .toList();

        // ✅ 주소 조립 사용
        String address = composeAddress(pr);

        // 무관(0/0)일 때는 null, 아니면 DTO
        PreferredAgesDto ages = (pr.getAgeMin() == 0 && pr.getAgeMax() == 0)
                ? null
                : new PreferredAgesDto(pr.getAgeMin(), pr.getAgeMax());

        return ProjectEditPageResponse.builder()
                .title(pr.getTitle())
                .teamStatus(pr.getTeamStatus().name())
                .bannerImageUrl(pr.getBannerImageUrl())
                .traits(pr.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .capacity(pr.getCapacity())
                .applicantCount(applicantCount)
                .mode(pr.getProjectMode().name().toLowerCase())
                .address(address)
                .preferredAges(ages)
                .expectedMonth(pr.getExpectedMonths())
                .startDate(pr.getStartDate())
                .detail(pr.getContentMd())
                .leaderPosition(resolveLeaderPositionName(projectId))
                .positions(positions)
                .build();
    }

    /* =========================
     *  수정 저장 (업데이트 방식)
     * ========================= */
    public ProjectUpdateResultResponse updateProject(Long projectId,
                                                     ProjectRecruitmentUpdateRequest req,
                                                     MultipartFile bannerImage,
                                                     CustomUserDetails me) {
        if (me == null || me.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);

        ProjectRecruitment pr = recruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROJECT_NOT_FOUND));

        // 리더만 수정
        if (!Objects.equals(pr.getUser().getId(), me.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // 과거 시작일 금지
        if (req.getExpectedStart() != null && req.getExpectedStart().isBefore(LocalDate.now())) {
            throw new GlobalException(ErrorCode.INVALID_START_DATE);
        }

        // 위치/포지션/리더포지션/연령 검증
        if (req.getMode() == ProjectMode.OFFLINE) {
            LocationDto loc = req.getLocation();
            if (loc == null || loc.getLatitude() == null || loc.getLongitude() == null) {
                throw new GlobalException(ErrorCode.INVALID_LOCATION);
            }
        }
        if (req.getPositions() == null || req.getPositions().isEmpty()) {
            throw new GlobalException(ErrorCode.INVALID_POSITIONS);
        }
        // 요청 포지션 정규화(중복/공백 제거)
        List<String> desiredPositions = req.getPositions().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (!desiredPositions.contains(req.getLeaderPosition())) {
            throw new GlobalException(ErrorCode.INVALID_LEADER_POSITION);
        }

        // capacity <= unique positions
        if (req.getCapacity() != null && req.getCapacity() > desiredPositions.size()) {
            throw new GlobalException(ErrorCode.INVALID_CAPACITY_POSITIONS);
        }

        // 연령 무관(null) 또는 10단위 강제
        int ageMin;
        int ageMax;
        if (req.getPreferredAges() == null) {
            ageMin = 0;
            ageMax = 0;
        } else {
            ageMin = req.getPreferredAges().getAgeMin();
            ageMax = req.getPreferredAges().getAgeMax();
            if (ageMin > ageMax) throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
            if (ageMin % 10 != 0 || ageMax % 10 != 0) {
                throw new GlobalException(ErrorCode.INVALID_AGE_BUCKET);
            }
        }

        // 배너: 파일 > 요청 URL > 기존 > 기본
        String bannerUrl = resolveBannerUrl(bannerImage, req.getBannerImageUrl(), pr.getBannerImageUrl(), req.getTitle());

        // 위치 업데이트 (카카오 필드 개별 저장)
        boolean offline = req.getMode() == ProjectMode.OFFLINE;
        if (offline) {
            pr = applyOfflineLocation(pr, req.getLocation());
        } else {
            pr = clearLocation(pr);
        }

        // 기본 필드 업데이트 (toBuilder)
        pr = pr.toBuilder()
                .title(req.getTitle())
                .teamStatus(req.getTeamStatus())
                .startDate(req.getExpectedStart())
                .expectedMonths(req.getExpectedMonth())
                .projectMode(req.getMode())
                .bannerImageUrl(bannerUrl)
                .contentMd(req.getDetail())
                .capacity(req.getCapacity())
                .ageMin(ageMin)
                .ageMax(ageMax)
                .build();

        // Traits 머지(단순 추가/제거)
        mergeTraits(pr, req.getTraits());

        // Positions 머지(요청에 없는 것은 참조 없을 때만 삭제, 있으면 에러)
        mergePositionsStrict(pr, desiredPositions);

        // 리더 포지션 동기화
        syncLeaderPositionTo(pr, req.getLeaderPosition());

        ProjectRecruitment saved = recruitmentRepository.save(pr);

        return buildUpdateResult(saved, me);
    }

    /* ---------- merge helpers ---------- */

    private void mergeTraits(ProjectRecruitment pr, List<String> incoming) {
        List<String> desired = (incoming == null) ? List.of() : incoming.stream()
                .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank()).distinct().toList();

        Map<String, ProjectRecruitmentTrait> current = pr.getTraits().stream()
                .collect(Collectors.toMap(ProjectRecruitmentTrait::getTraitName, t -> t, (a, b) -> a));

        for (String name : desired) {
            if (!current.containsKey(name)) {
                pr.getTraits().add(ProjectRecruitmentTrait.builder()
                        .projectRecruitment(pr)
                        .traitName(name)
                        .build());
            }
        }
        pr.getTraits().removeIf(t -> !desired.contains(t.getTraitName()));
    }

    private void mergePositionsStrict(ProjectRecruitment pr, List<String> desired) {
        Map<String, ProjectRecruitmentPosition> byName = pr.getPositions().stream()
                .collect(Collectors.toMap(ProjectRecruitmentPosition::getPositionName, p -> p, (a, b) -> a));

        for (String name : desired) {
            if (!byName.containsKey(name)) {
                pr.getPositions().add(ProjectRecruitmentPosition.builder()
                        .projectRecruitment(pr)
                        .positionName(name)
                        .build());
            }
        }

        List<ProjectRecruitmentPosition> toRemove = pr.getPositions().stream()
                .filter(pos -> !desired.contains(pos.getPositionName()))
                .toList();

        for (ProjectRecruitmentPosition pos : toRemove) {
            long refByParticipants = participantRepository.countByPosition_IdAndDeletedAtIsNull(pos.getId());
            long refByApplications = applicationRepository.countByPosition_Id(pos.getId());
            if (refByParticipants > 0 || refByApplications > 0) {
                throw new GlobalException(ErrorCode.POSITION_IN_USE);
            }
        }
        pr.getPositions().removeIf(pos -> !desired.contains(pos.getPositionName()));
    }

    /** 리더 참가자의 포지션을 새 이름으로 동기화 */
    private void syncLeaderPositionTo(ProjectRecruitment pr, String leaderPositionName) {
        if (leaderPositionName == null) return;

        ProjectRecruitmentPosition target = pr.getPositions().stream()
                .filter(p -> p.getPositionName().equals(leaderPositionName))
                .findFirst()
                .orElse(null);
        if (target == null) return;

        participantRepository
                .findFirstByPosition_ProjectRecruitment_IdAndRoleAndDeletedAtIsNull(pr.getId(), ParticipantRole.LEADER)
                .ifPresent(leader -> leader.changePosition(target));
    }

    /* ---------- 주소/위치 helpers (여기만 변경) ---------- */

    /** 카카오 road_address 필드를 엔티티 각 컬럼에 저장 */
    private ProjectRecruitment applyOfflineLocation(ProjectRecruitment pr, LocationDto loc) {
        return pr.toBuilder()
                .region1depthName(loc.getRegion1depthName())
                .region2depthName(loc.getRegion2depthName())
                .region3depthName(loc.getRegion3depthName())
                .roadName(loc.getRoadName())
                .mainBuildingNo(loc.getMainBuildingNo())
                .subBuildingNo(loc.getSubBuildingNo())
                .zoneNo(loc.getZoneNo())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .build();
    }

    /** ONLINE 등 위치 초기화 */
    private ProjectRecruitment clearLocation(ProjectRecruitment pr) {
        return pr.toBuilder()
                .region1depthName(null)
                .region2depthName(null)
                .region3depthName(null)
                .roadName(null)
                .mainBuildingNo(null)
                .subBuildingNo(null)
                .zoneNo(null)
                .latitude(null)
                .longitude(null)
                .build();
    }

    /** 응답용 전체 주소 만들기: "r1 r2 r3 road main-sub" */
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

    /* ---------- banner helper ---------- */
    private String resolveBannerUrl(MultipartFile file, String requestUrl, String currentUrl, String titleForDefault) {
        if (file != null && !file.isEmpty()) {
            try {
                return fileService.upload(file);
            } catch (IOException e) {
                throw new GlobalException(ErrorCode.BANNER_UPLOAD_FAILED);
            }
        }
        if (requestUrl != null && !requestUrl.isBlank()) return requestUrl.trim();
        if (currentUrl != null && !currentUrl.isBlank()) return currentUrl;
        return bannerImageService.generateBannerImageUrl(
                (titleForDefault == null ? "PROJECT" : titleForDefault), "PROJECT", 1200, 600
        );
    }

    /* ---------- response builders (기존) ---------- */

    private String resolveLeaderPositionName(Long projectId) {
        return participantRepository
                .findFirstByPosition_ProjectRecruitment_IdAndRoleAndDeletedAtIsNull(projectId, ParticipantRole.LEADER)
                .map(pp -> pp.getPosition() != null ? pp.getPosition().getPositionName() : null)
                .orElse(null);
    }

    private ProjectUpdateResultResponse.LeaderBlock resolveLeader(Long projectId, CustomUserDetails me) {
        return participantRepository
                .findFirstByPosition_ProjectRecruitment_IdAndRoleAndDeletedAtIsNull(projectId, ParticipantRole.LEADER)
                .map(pp -> {
                    User u = pp.getUser();
                    boolean mine = me != null && me.getUser() != null && Objects.equals(u.getId(), me.getUser().getId());
                    return ProjectUpdateResultResponse.LeaderBlock.builder()
                            .userId(u.getId())
                            .nickname(u.getNickname())
                            .profileImageUrl(u.getProfileImageUrl())
                            .mainPosition(null)
                            .temperature(null)
                            .decidedPosition(pp.getPosition() != null ? pp.getPosition().getPositionName() : null)
                            .IsMine(mine)
                            .chatRoomId(null)
                            .dmRequestPending(false)
                            .build();
                })
                .orElse(null);
    }

    private List<ProjectUpdateResultResponse.ParticipantBlock> resolveParticipants(Long projectId, CustomUserDetails me) {
        Long meId = (me != null && me.getUser() != null) ? me.getUser().getId() : null;

        return participantRepository.findByPosition_ProjectRecruitment_IdAndDeletedAtIsNull(projectId).stream()
                .filter(pp -> pp.getRole() == ParticipantRole.MEMBER) // 리더 제외
                .map(pp -> {
                    User u = pp.getUser();
                    return ProjectUpdateResultResponse.ParticipantBlock.builder()
                            .userId(u.getId())
                            .nickname(u.getNickname())
                            .profileImageUrl(u.getProfileImageUrl())
                            .mainPosition(null)
                            .temperature(null)
                            .decidedPosition(pp.getPosition() != null ? pp.getPosition().getPositionName() : null)
                            .IsMine(meId != null && Objects.equals(meId, u.getId()))
                            .chatRoomId(null)
                            .dmRequestPending(false)
                            .build();
                })
                .toList();
    }

    private ProjectUpdateResultResponse buildUpdateResult(ProjectRecruitment pr, CustomUserDetails me) {
        Long projectId = pr.getId();

        long applicantCount = applicationRepository.countAllByProjectId(projectId);

        Map<String, Long> applied = applicationRepository.countAppliedByPosition(projectId).stream()
                .collect(Collectors.toMap(
                        ProjectApplicationRepository.PositionCountProjection::getPositionName,
                        ProjectApplicationRepository.PositionCountProjection::getCnt
                ));
        Map<String, Long> confirmed = applicationRepository.countApprovedByPosition(projectId).stream()
                .collect(Collectors.toMap(
                        ProjectApplicationRepository.PositionCountProjection::getPositionName,
                        ProjectApplicationRepository.PositionCountProjection::getCnt
                ));

        Long meId = (me != null && me.getUser() != null) ? me.getUser().getId() : null;
        boolean myApplied = (meId != null) && applicationRepository
                .existsByUser_IdAndPosition_ProjectRecruitment_Id(meId, projectId);
        boolean myApproved = (meId != null) && applicationRepository
                .existsByUser_IdAndPosition_ProjectRecruitment_IdAndStatus(meId, projectId, ApplicationStatus.APPROVED);

        List<ProjectUpdateResultResponse.PositionItem> positionItems = pr.getPositions().stream()
                .map(p -> ProjectUpdateResultResponse.PositionItem.builder()
                        .position(p.getPositionName())
                        .applied(applied.getOrDefault(p.getPositionName(), 0L))
                        .confirmed(confirmed.getOrDefault(p.getPositionName(), 0L))
                        .IsApplied(myApplied)
                        .IsApproved(myApproved)
                        .IsAvailable(pr.getTeamStatus() == TeamStatus.RECRUITING)
                        .build())
                .toList();

        // ✅ 응답 주소도 조립 사용
        String address = composeAddress(pr);

        boolean isMine = meId != null && Objects.equals(pr.getUser().getId(), meId);

        // 무관(0/0)일 때 null
        PreferredAgesDto prefAges =
                (pr.getAgeMin() == 0 && pr.getAgeMax() == 0)
                        ? null
                        : new PreferredAgesDto(pr.getAgeMin(), pr.getAgeMax());

        return ProjectUpdateResultResponse.builder()
                .projectId(projectId)
                .IsMine(isMine)
                .title(pr.getTitle())
                .teamStatus(pr.getTeamStatus().name())
                .bannerImageUrl(pr.getBannerImageUrl())
                .traits(pr.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .capacity(pr.getCapacity())
                .applicantCount(applicantCount)
                .mode(pr.getProjectMode().name().toLowerCase())
                .address(address)
                .preferredAges(prefAges)
                .expectedMonth(pr.getExpectedMonths())
                .startDate(pr.getStartDate())
                .detail(pr.getContentMd())
                .positions(positionItems)
                .leader(resolveLeader(projectId, me))
                .participants(resolveParticipants(projectId, me))
                .build();
    }
}