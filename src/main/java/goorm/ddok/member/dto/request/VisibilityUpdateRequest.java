package goorm.ddok.member.dto.request;

import lombok.Getter;

@Getter
public class VisibilityUpdateRequest {
    // User에 컬럼이 아직 없어 no-op
    private Boolean isPublic;
}