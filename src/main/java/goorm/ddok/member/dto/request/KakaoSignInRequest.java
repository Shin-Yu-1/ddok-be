package goorm.ddok.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KakaoSignInRequest(
        @NotBlank String authorizationCode,
        @NotBlank String redirectUri
) {}