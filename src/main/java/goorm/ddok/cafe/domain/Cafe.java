package goorm.ddok.cafe.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cafe",
        indexes = {
                @Index(name = "idx_cafe_name", columnList = "name"),
                @Index(name = "idx_cafe_lat_lng", columnList = "activity_latitude, activity_longitude")
        })
public class Cafe {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120, nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String bannerImageUrl;

    @Column(name = "region_1depth_name", length = 50) private String region1depthName;
    @Column(name = "region_2depth_name", length = 50) private String region2depthName;
    @Column(name = "region_3depth_name", length = 50) private String region3depthName;

    /** 도로명 */
    @Column(name = "road_name", length = 50)
    private String roadName;

    /** 건물 본번 */
    @Column(name = "main_building_no", length = 20)
    private String mainBuildingNo;

    /** 건물 부번 */
    @Column(name = "sub_building_no", length = 20)
    private String subBuildingNo;

    /** 우편번호 (출력에는 사용하지 않음) */
    @Column(name = "zone_no", length = 50)
    private String zoneNo;

    @Column(name = "kakao_place_id", length = 64)
    private String kakaoPlaceId;

    @Column(name = "activity_latitude", precision = 9, scale = 6)
    private BigDecimal activityLatitude;

    @Column(name = "activity_longitude", precision = 9, scale = 6)
    private BigDecimal activityLongitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * LocationDto 형태의 “전체 도로명 주소” (우편번호 제외):
     *   {r1} {r2} {r3} {roadName} {main}-{sub}
     *   예) "서울특별시 강남구 테헤란로 123-4"
     */
    public String composeFullAddress() {
        String r1   = safe(region1depthName);
        String r2   = safe(region2depthName);
        String r3   = safe(region3depthName);
        String road = safe(roadName);

        String main = safe(mainBuildingNo);
        String sub  = safe(subBuildingNo);
        String num  = main.isEmpty() ? "" : (sub.isEmpty() ? main : (main + "-" + sub));

        String base = (r1 + " " + r2 + " " + r3 + " " + road + " " + num)
                .trim()
                .replaceAll("\\s+", " ");

        return base.isEmpty() ? "-" : base;
    }

    private static String safe(String s) { return (s == null) ? "" : s.trim(); }
}