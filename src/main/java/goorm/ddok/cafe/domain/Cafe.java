package goorm.ddok.cafe.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "cafe",
        indexes = {
                @Index(name = "idx_cafe_name", columnList = "name"),
                @Index(name = "idx_cafe_lat_lng", columnList = "activity_latitude, activity_longitude")
        }
)
public class Cafe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120, nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String bannerImageUrl;

    @Column(length = 50)
    private String region1depthName;

    @Column(length = 50)
    private String region2depthName;

    @Column(length = 50)
    private String region3depthName;

    @Column(length = 50)
    private String roadName;

    @Column(length = 50)
    private String zoneNo;

    @Column(length = 64)
    private String kakaoPlaceId;

    // numeric(9,6)
    @Column(precision = 9, scale = 6)
    private BigDecimal activityLatitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal activityLongitude;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Column
    private Instant deletedAt;
}