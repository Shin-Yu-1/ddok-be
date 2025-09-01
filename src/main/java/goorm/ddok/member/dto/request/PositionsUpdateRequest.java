package goorm.ddok.member.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PositionsUpdateRequest {
    private String mainPosition;
    private List<String> subPositions; // 최대 2개 반영 (서비스에서 제한)
}