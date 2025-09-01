package goorm.ddok.member.dto.request;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LocationUpdateRequest {
    @Valid
    private Location location;

    @Getter @Setter
    public static class Location {
        private Double latitude;
        private Double longitude;
        private String address; // 카카오 세부필드는 필요 시 확장
    }
}