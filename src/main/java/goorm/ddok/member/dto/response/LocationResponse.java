package goorm.ddok.member.dto.response;

import goorm.ddok.member.domain.UserLocation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Optional;

@Schema(
        name = "LocationResponse",
        description = "사용자 위치 정보 응답 DTO"
)
@Builder
public record LocationResponse(
        @Schema(description = "위도", example = "37.5665", minimum = "-90.0", maximum = "90.0",
                type = "number", format = "BigDecimal", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal latitude,

        @Schema(description = "경도", example = "126.9780", minimum = "-180.0", maximum = "180.0",
                type = "number", format = "BigDecimal", accessMode = Schema.AccessMode.READ_ONLY)
        BigDecimal longitude,

        @Schema(description = "주소", example = "전북 익산시 부송동 망산길 11-17 (54547)",
                type = "string", accessMode = Schema.AccessMode.READ_ONLY)
        String address,

        @Schema(description = "광역시/도 (region_1depth_name)", example = "전북")
        String region1depthName,

        @Schema(description = "시/군/구 (region_2depth_name)", example = "익산시")
        String region2depthName,

        @Schema(description = "동/읍/면 (region_3depth_name)", example = "부송동")
        String region3depthName,

        @Schema(description = "도로명 (road_name)", example = "망산길")
        String roadName,

        @Schema(description = "건물 본번 (main_building_no)", example = "11")
        String mainBuildingNo,

        @Schema(description = "건물 부번 (sub_building_no)", example = "17")
        String subBuildingNo,

        @Schema(description = "우편번호(필요 시) (zone_no)", example = "54547")
        String zoneNo
) {
    public static LocationResponse from(UserLocation loc) {
        if (loc == null) return null;
        return new LocationResponse(
                loc.getActivityLatitude(),
                loc.getActivityLongitude(),
                composeAddress(loc),
                loc.getRegion1DepthName(),
                loc.getRegion2DepthName(),
                loc.getRegion3DepthName(),
                loc.getRoadName(),
                loc.getMainBuildingNo(),
                loc.getSubBuildingNo(),
                loc.getZoneNo()
        );
    }

    private static String composeAddress(UserLocation loc) {
        String r1   = Optional.ofNullable(loc.getRegion1DepthName()).orElse("");
        String r2   = Optional.ofNullable(loc.getRegion2DepthName()).orElse("");
        String r3   = Optional.ofNullable(loc.getRegion3DepthName()).orElse("");
        String road = Optional.ofNullable(loc.getRoadName()).orElse("");
        String main = Optional.ofNullable(loc.getMainBuildingNo()).orElse("");
        String sub  = Optional.ofNullable(loc.getSubBuildingNo()).orElse("");

        StringBuilder sb = new StringBuilder();
        if (!r1.isBlank())   sb.append(r1).append(" ");
        if (!r2.isBlank())   sb.append(r2).append(" ");
        if (!r3.isBlank())   sb.append(r3).append(" ");
        if (!road.isBlank()) sb.append(road).append(" ");
        if (!main.isBlank() && !sub.isBlank()) sb.append(main).append("-").append(sub);
        else if (!main.isBlank())              sb.append(main);

        String s = sb.toString().trim().replaceAll("\\s+", " ");
        return s.isBlank() ? "-" : s;
    }
}
