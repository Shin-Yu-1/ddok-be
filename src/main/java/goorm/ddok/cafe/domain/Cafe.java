package goorm.ddok.cafe.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Builder
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

    @Column(name = "region_1depth_name",length = 50) private String region1depthName;
    @Column(name = "region_2depth_name",length = 50) private String region2depthName;
    @Column(name = "region_3depth_name",length = 50) private String region3depthName;
    @Column(name = "road_name",length = 50) private String roadName;
    @Column(name = "zone_no",length = 50) private String zoneNo;

    @Column(name = "kakao_place_id",length = 64)
    private String kakaoPlaceId;

    @Column(name="activity_latitude", precision = 9, scale = 6)
    private BigDecimal activityLatitude;

    @Column(name="activity_longitude", precision = 9, scale = 6)
    private BigDecimal activityLongitude;

    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}