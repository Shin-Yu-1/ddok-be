package goorm.ddok.team.dto.response;

import goorm.ddok.project.domain.ProjectApplication;
import goorm.ddok.study.domain.StudyApplication;
import goorm.ddok.team.domain.ApplicantStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamApplicantResponse {

    private Long applicantId;
    /* Study일 경우 null */
    private String appliedPosition;

    @Enumerated(EnumType.STRING)
    private ApplicantStatus status;

    private Instant appliedAt;
    private boolean IsMine;
    private TeamApplicantUserResponse user;

    // StudyApplication -> DTO
    public static TeamApplicantResponse fromStudy(StudyApplication entity, Long currentUserId) {
        return TeamApplicantResponse.builder()
                .applicantId(entity.getId())
                .appliedPosition(null) // Study는 appliedPosition 없음
                .status(ApplicantStatus.from(entity.getApplicationStatus()))
                .appliedAt(entity.getCreatedAt())
                .IsMine(entity.getUser().getId().equals(currentUserId))
                .user(TeamApplicantUserResponse.from(entity.getUser()))
                .build();
    }

    // ProjectApplication -> DTO
    public static TeamApplicantResponse fromProject(ProjectApplication entity, Long currentUserId) {
        return TeamApplicantResponse.builder()
                .applicantId(entity.getId())
                .appliedPosition(entity.getPosition().getPositionName())
                .status(ApplicantStatus.from(entity.getStatus()))
                .appliedAt(entity.getCreatedAt())
                .IsMine(entity.getUser().getId().equals(currentUserId))
                .user(TeamApplicantUserResponse.from(entity.getUser()))
                .build();
    }
}
