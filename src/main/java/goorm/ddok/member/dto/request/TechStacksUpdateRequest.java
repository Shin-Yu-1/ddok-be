package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Schema(name = "TechStacksUpdateRequest", description = "기술 스택 전체 치환 요청", example = """
    { "techStacks": ["Java","Spring","JPA"] }
    """)
public class TechStacksUpdateRequest {
    private List<String> techStacks;
}