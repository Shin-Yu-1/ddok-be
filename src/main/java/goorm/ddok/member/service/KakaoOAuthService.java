package goorm.ddok.member.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret:}")
    private String clientSecret;

    @Value("${oauth.kakao.token-uri:https://kauth.kakao.com/oauth/token}")
    private String tokenUri;

    @Value("${oauth.kakao.userinfo-uri:https://kapi.kakao.com/v2/user/me}")
    private String userInfoUri;

    private final ObjectMapper om = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();

    public String exchangeCodeForAccessToken(String authorizationCode, String redirectUri) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isBlank()) body.add("client_secret", clientSecret);
        body.add("code", authorizationCode);
        body.add("redirect_uri", redirectUri);

        try {
            ResponseEntity<String> res = rest.postForEntity(tokenUri, new HttpEntity<>(body, h), String.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Kakao token exchange failed: " + res.getStatusCode());
            }
            return om.readTree(res.getBody()).get("access_token").asText();
        } catch (org.springframework.web.client.HttpClientErrorException e) {

            String resp = e.getResponseBodyAsString();
            System.err.println("[KAKAO TOKEN ERROR] status=" + e.getStatusCode() + " body=" + resp);
            if (e.getStatusCode().value() == 400 || e.getStatusCode().value() == 401) {
                if (resp != null && (
                        resp.contains("invalid_grant") ||
                                resp.contains("Invalid authorization code") ||
                                resp.contains("KOE320")
                )) {
                    throw new GlobalException(ErrorCode.KAKAO_INVALID_CODE);
                }
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Invalid token response", e);
        }
    }

    public KakaoUser fetchUser(String kakaoAccessToken) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(kakaoAccessToken);

        ResponseEntity<String> res = rest.exchange(userInfoUri, HttpMethod.GET, new HttpEntity<>(h), String.class);
        if (!res.getStatusCode().is2xxSuccessful()) throw new RuntimeException("Kakao userinfo failed");

        try {
            JsonNode root = om.readTree(res.getBody());
            String id = root.get("id").asText();

            JsonNode acc = root.get("kakao_account");
            String email = (acc != null && acc.has("email")) ? acc.get("email").asText(null) : null;

            String nickname = null, profile = null;
            if (acc != null && acc.has("profile")) {
                JsonNode p = acc.get("profile");
                if (p.has("nickname")) nickname = p.get("nickname").asText();
                if (p.has("profile_image_url")) profile = p.get("profile_image_url").asText();
            }
            return new KakaoUser(id, email, nickname, profile);
        } catch (Exception e) {
            throw new RuntimeException("Invalid userinfo response", e);
        }
    }

    public record KakaoUser(String kakaoId, String email, String nickname, String profileImageUrl) {}
}