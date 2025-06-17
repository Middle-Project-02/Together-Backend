package com.together.server.infra.smartchoice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmartChoiceClient {

    @Qualifier("smartChoiceWebClient")
    private final WebClient smartChoiceWebClient;
    private final SmartChoiceXmlParser xmlParser;

    @Value("${smartchoice.api.key}")
    private String authKey;

    public List<Map<String, String>> getPlans(String voice, String data, String sms, String age, String type) {
        String url = "/openAPI.xml";

        try {
            // API 호출 전 로깅
            log.info("SmartChoice API 호출 - voice: {}, data: {}, sms: {}, age: {}, type: {}, authKey: {}",
                    voice, data, sms, age, type, authKey);

            String xmlResponse = smartChoiceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("voice", voice)
                            .queryParam("data", data)
                            .queryParam("sms", sms)
                            .queryParam("age", age)
                            .queryParam("type", type)
                            .queryParam("authkey", authKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("SmartChoice API 응답: {}", xmlResponse);

            if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
                log.error("SmartChoice API 응답이 비어있습니다.");
                return List.of();
            }

            if (xmlResponse.trim().toLowerCase().startsWith("<!doctype") ||
                    xmlResponse.trim().toLowerCase().startsWith("<html")) {
                log.error("SmartChoice API에서 HTML 응답을 받았습니다. XML이 아닙니다: {}", xmlResponse);
                return List.of();
            }

            return xmlParser.parsePlans(xmlResponse);

        } catch (Exception e) {
            log.error("SmartChoice API 호출 실패", e);
            return List.of(); // 빈 리스트 반환
        }
    }
}