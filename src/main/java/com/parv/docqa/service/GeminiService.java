package com.parv.docqa.service;

import com.parv.docqa.config.GeminiConfig;
import com.parv.docqa.dto.ChatMessage;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;

    public GeminiService(RestTemplate restTemplate, GeminiConfig geminiConfig) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
    }

    public String callGemini(String prompt) {

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String urlWithKey = geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, entity, Map.class);

        // Extract answer from Gemini response
        Map candidates = (Map) ((List) response.getBody().get("candidates")).get(0);
        Map content = (Map) candidates.get("content");
        Map part = (Map) ((List) content.get("parts")).get(0);
        String answer = (String) part.get("text");
        return answer;
    }

    public String callGemini2(String question, List<ChatMessage> history) {
        List<Map<String, Object>> contents;
        if (history != null && history.size() > 1) {
            contents = history.stream().map(msg -> Map.<String, Object>of(
                    "role", msg.getRole(),
                    "parts", List.of(Map.of("text", msg.getContent()))
            )).collect(Collectors.toList());
        } else {
            contents = List.of(Map.<String, Object>of("role", history.get(0).getRole(), "parts", List.of(Map.of("text", history.get(0).getContent()))));
        }
        Map<String, Object> body = Map.of("contents", contents);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String urlWithKey = geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, entity, Map.class);
        Map candidates = (Map) ((List) response.getBody().get("candidates")).get(0);
        Map content = (Map) candidates.get("content");
        Map part = (Map) ((List) content.get("parts")).get(0);
        return (String) part.get("text");
    }
}
