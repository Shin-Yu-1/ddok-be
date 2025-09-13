package goorm.ddok.notification.event;

import goorm.ddok.team.domain.TeamType;

public record TeamMemberExitEvent(
        Long teamId,
        Long actorUserId,
        TeamType teamType,
        Long recruitmentId,
        String teamTitle,
        Reason reason
) {
    public enum Reason { WITHDRAWN, EXPELLED }
}
