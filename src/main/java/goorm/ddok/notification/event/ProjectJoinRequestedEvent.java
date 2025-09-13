package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class ProjectJoinRequestedEvent {
    /** 신청 엔티티 ID (ProjectApplication.id) */
    private final Long applicationId;

    /** 신청자 유저 ID */
    private final Long applicantUserId;

    /** 신청자 닉네임(메시지 구성용) */
    private final String applicantNickname;

    /** 프로젝트 ID */
    private final Long projectId;

    /** 프로젝트 제목(메시지 구성용) */
    private final String projectTitle;

    /** 신청 포지션명 */
    private final String appliedPosition;

    /** 프로젝트 소유자(알림 수신자) 유저 ID */
    private final Long ownerUserId;
}
