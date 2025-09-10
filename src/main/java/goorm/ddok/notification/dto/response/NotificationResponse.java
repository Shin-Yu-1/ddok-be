package goorm.ddok.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import goorm.ddok.notification.domain.Notification;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class NotificationResponse {
    String id;
    String type;
    String message;
    boolean IsRead;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant createdAt;

    String userId;
    String userNickname;

    String projectId;
    String projectTitle;
    String studyId;
    String studyTitle;
    String achievementName;
    String teamId;
    String teamName;

    BigDecimal userTemperature;

    public static NotificationResponse from(Notification n) {
        return from(n, null);
    }

    // ★ 온도 주입용 오버로드
    public static NotificationResponse from(Notification n, BigDecimal userTemperature) {
        return NotificationResponse.builder()
                .id(String.valueOf(n.getId()))
                .type(n.getType().name())
                .message(n.getMessage())
                .IsRead(Boolean.TRUE.equals(n.getRead()))
                .createdAt(n.getCreatedAt())
                .userId(n.getApplicantUserId() != null ? String.valueOf(n.getApplicantUserId()) : null)
                .userNickname(null) // 필요 시 채우기
                .projectId(n.getProjectId() != null ? String.valueOf(n.getProjectId()) : null)
                .projectTitle(n.getProjectTitle())
                .studyId(n.getStudyId() != null ? String.valueOf(n.getStudyId()) : null)
                .studyTitle(n.getStudyTitle())
                .achievementName(n.getAchievementName())
                .teamId(n.getTeamId() != null ? String.valueOf(n.getTeamId()) : null)
                .teamName(n.getTeamName())
                .userTemperature(userTemperature) // ★ 주입
                .build();
    }
}
