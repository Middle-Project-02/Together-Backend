package com.together.server.application.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.together.server.application.auth.response.KakaoUserResponse;
import com.together.server.infra.oauth.KakaoOAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoService {
    private final KakaoOAuthClient kakaoOAuthClient;
    private final ObjectMapper objectMapper;

    public KakaoUserResponse getKakaoUser(String accessToken) {
        ResponseEntity<String> response = kakaoOAuthClient.getUserInfo(accessToken);

        try {
            JsonNode node = objectMapper.readTree(response.getBody());
            String id = node.get("id").asText();
            String email = node.path("kakao_account").path("email").asText();
            String nickname = node.path("properties").path("nickname").asText();

            return new KakaoUserResponse(id, email, nickname);
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보를 파싱하는 중 오류가 발생했습니다.");
        }
    }
}
