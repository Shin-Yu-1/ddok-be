package goorm.ddok.member.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class TechStacksUpdateRequest {
    private List<String> techStacks;
}