package goorm.ddok.notification.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPayload {
    private String id;
    private String type;
    private String message;
    private boolean isRead;
    private Instant createdAt;

    private String actorUserId;
    private String actorNickname;
    private BigDecimal actorTemperature;

    @Deprecated private String userId;
    @Deprecated private String userNickname;

    private String projectId;
    private String projectTitle;
    private String studyId;
    private String studyTitle;
    private String achievementName;
    private String teamId;
    private String teamName;
}
