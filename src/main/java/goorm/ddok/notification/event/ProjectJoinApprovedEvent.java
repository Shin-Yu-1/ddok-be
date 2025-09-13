package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class ProjectJoinApprovedEvent {
    /** 신청 엔티티 ID (선택) */
    private final Long applicationId;

    /** 승인 대상자(신청자) — 알림 수신자 */
    private final Long applicantUserId;

    /** 프로젝트 식별자/제목 — 메시지 구성 및 프론트 표시용 */
    private final Long projectId;
    private final String projectTitle;

    /** 승인자(프로젝트 리더) — 감사 로그/추적용 */
    private final Long approverUserId;
}
