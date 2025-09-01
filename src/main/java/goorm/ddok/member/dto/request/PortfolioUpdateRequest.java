package goorm.ddok.member.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PortfolioUpdateRequest {
    private List<Link> portfolio;

    @Getter @Setter
    public static class Link {
        private String linkTitle;
        private String link;
    }
}