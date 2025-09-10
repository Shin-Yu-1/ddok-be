package goorm.ddok.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import goorm.ddok.member.domain.User;
import goorm.ddok.notification.domain.Notification;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
@JsonInclude(Include.NON_NULL)
public class NotificationResponse {
    String id;
    String type;
    String message;
    boolean IsRead;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant createdAt;

    String actorUserId;
    String actorNickname;
    BigDecimal actorTemperature;

    /** @deprecated 프론트 마이그레이션 완료 후 제거 예정 */
    @Deprecated String userId;        // = actorUserId
    /** @deprecated 프론트 마이그레이션 완료 후 제거 예정 */
    @Deprecated String userNickname;  // = actorNickname

    String projectId; String projectTitle;
    String studyId;   String studyTitle;
    String achievementName;
    String teamId;    String teamName;

    public static NotificationResponse from(Notification n, @Nullable User actor, @Nullable BigDecimal temp) {
        String actorId = resolveActorId(n);
        String actorNick = actor != null ? actor.getNickname() : null;

        return NotificationResponse.builder()
                .id(String.valueOf(n.getId()))
                .type(n.getType().name())
                .message(n.getMessage())
                .IsRead(Boolean.TRUE.equals(n.getRead()))
                .createdAt(n.getCreatedAt())
                .actorUserId(actorId)
                .actorNickname(actorNick)
                .actorTemperature(temp)
                // 호환 필드 (임시)
                .userId(actorId)
                .userNickname(actorNick)
                .projectId(n.getProjectId() != null ? String.valueOf(n.getProjectId()) : null)
                .projectTitle(n.getProjectTitle())
                .studyId(n.getStudyId() != null ? String.valueOf(n.getStudyId()) : null)
                .studyTitle(n.getStudyTitle())
                .achievementName(n.getAchievementName())
                .teamId(n.getTeamId() != null ? String.valueOf(n.getTeamId()) : null)
                .teamName(n.getTeamName())
                .build();
    }

    private static String resolveActorId(Notification n) {
        return switch (n.getType()) {
            case PROJECT_JOIN_REQUEST, STUDY_JOIN_REQUEST ->
                    n.getApplicantUserId() != null ? String.valueOf(n.getApplicantUserId()) : null;
            case PROJECT_JOIN_APPROVED, PROJECT_JOIN_REJECTED,
                 STUDY_JOIN_APPROVED, STUDY_JOIN_REJECTED ->
                    n.getRequesterUserId() != null ? String.valueOf(n.getRequesterUserId()) : null;
            default -> null;
        };
    }
}
