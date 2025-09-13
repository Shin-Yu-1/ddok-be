package goorm.ddok.member.service;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.dto.response.PlayerProfileMapItemResponse;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.project.domain.TeamStatus;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlayerProfileMapService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final StudyRecruitmentRepository studyRecruitmentRepository;
    private final UserRepository userRepository;

    public List<PlayerProfileMapItemResponse> getProfileMapInBounds(
            BigDecimal swLat, BigDecimal swLng,
            BigDecimal neLat, BigDecimal neLng,
            BigDecimal centerLat, BigDecimal centerLng,
            Long userId
    ){

        if (!userRepository.existsById(userId)) {
            throw new GlobalException(ErrorCode.USER_NOT_FOUND);
        }

        validateBounds(swLat, swLng, neLat, neLng);

        List<PlayerProfileMapItemResponse> result = new ArrayList<>();

        var projectRows = projectRecruitmentRepository.findAllInBoundsForProfile(userId, swLat, neLat, swLng, neLng);
        if (projectRows != null) {
            List<PlayerProfileMapItemResponse> finalResult = result;
            projectRows.forEach(r -> finalResult.add(PlayerProfileMapItemResponse.builder()
                    .category("project")
                    .projectId(r.getId())
                    .title(r.getTitle())
                    .teamStatus(normalizeProjectTeamStatus(r.getTeamStatus()))
                    .location(LocationDto.builder()
                            .address(composeRoadAddress(
                                    r.getRegion1depthName(),
                                    r.getRegion2depthName(),
                                    r.getRegion3depthName(),
                                    r.getRoadName(),
                                    r.getMainBuildingNo(),
                                    r.getSubBuildingNo()))
                            .region1depthName(r.getRegion1depthName())
                            .region2depthName(r.getRegion2depthName())
                            .region3depthName(r.getRegion3depthName())
                            .roadName(r.getRoadName())
                            .mainBuildingNo(r.getMainBuildingNo())
                            .subBuildingNo(r.getSubBuildingNo())
                            .zoneNo(r.getZoneNo())
                            .latitude(r.getLatitude())
                            .longitude(r.getLongitude())
                            .build())
                    .build()));
        }

        var studyRows = studyRecruitmentRepository.findAllInBoundsForProfile(userId, swLat, neLat, swLng, neLng);
        if (studyRows != null) {
            List<PlayerProfileMapItemResponse> finalResult1 = result;
            studyRows.forEach(r -> finalResult1.add(PlayerProfileMapItemResponse.builder()
                    .category("study")
                    .studyId(r.getId())
                    .title(r.getTitle())
                    .teamStatus(normalizeStudyTeamStatus(r.getTeamStatus()))
                    .location(LocationDto.builder()
                            .address(composeRoadAddress(
                                    r.getRegion1depthName(),
                                    r.getRegion2depthName(),
                                    r.getRegion3depthName(),
                                    r.getRoadName(),
                                    r.getMainBuildingNo(),
                                    r.getSubBuildingNo()))
                            .region1depthName(r.getRegion1depthName())
                            .region2depthName(r.getRegion2depthName())
                            .region3depthName(r.getRegion3depthName())
                            .roadName(r.getRoadName())
                            .mainBuildingNo(r.getMainBuildingNo())
                            .subBuildingNo(r.getSubBuildingNo())
                            .zoneNo(r.getZoneNo())
                            .latitude(r.getLatitude())
                            .longitude(r.getLongitude())
                            .build())
                    .build()));
        }

        if (centerLat != null && centerLng != null && !result.isEmpty()) {
            final double cLat = centerLat.doubleValue();
            final double cLng = centerLng.doubleValue();
            result = result.stream()
                    .sorted(Comparator.comparingDouble(item -> {
                        var loc = item.getLocation();
                        return haversineKm(
                                cLat, cLng,
                                loc.getLatitude().doubleValue(),
                                loc.getLongitude().doubleValue()
                        );
                    }))
                    .toList();
        }

        if (result.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return result;
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return R * c;
    }

    public String composeRoadAddress(String r1, String r2, String r3,
                                     String road, String mainNo, String subNo) {
        if (road == null && r1 == null && r2 == null) return "-";
        var sb = new StringBuilder();
        if (r1 != null && !r1.isBlank()) sb.append(r1).append(" ");
        if (r2 != null && !r2.isBlank()) sb.append(r2).append(" ");
        if (r3 != null && !r3.isBlank()) sb.append(r3).append(" ");
        if (road != null && !road.isBlank()) sb.append(road).append(" ");

        String main = mainNo == null ? "" : mainNo.trim();
        String sub  = subNo == null ? "" : subNo.trim();
        if (!main.isEmpty() && !sub.isEmpty()) sb.append(main).append("-").append(sub);
        else if (!main.isEmpty()) sb.append(main);

        return sb.toString().trim();
    }

    public void validateBounds(BigDecimal swLat, BigDecimal swLng, BigDecimal neLat, BigDecimal neLng) {
        boolean hasAny = swLat != null || swLng != null || neLat != null || neLng != null;
        boolean hasAll = swLat != null && swLng != null && neLat != null && neLng != null;
        if (hasAny && !hasAll) {
            throw new GlobalException(ErrorCode.REQUIRED_PARAMETER_MISSING);
        }
        if (hasAll && (swLat.compareTo(neLat) > 0 || swLng.compareTo(neLng) > 0)) {
            throw new GlobalException(ErrorCode.INVALID_MAP_BOUNDS);
        }
    }

    private String normalizeProjectTeamStatus(goorm.ddok.project.domain.TeamStatus status) {
        if (status == null) return "ONGOING";
        return (status == TeamStatus.RECRUITING) ? "ONGOING" : status.name();
    }

    private String normalizeStudyTeamStatus(goorm.ddok.study.domain.TeamStatus status) {
        if (status == null) return "ONGOING";
        return (status == goorm.ddok.study.domain.TeamStatus.RECRUITING) ? "ONGOING" : status.name();
    }

}
