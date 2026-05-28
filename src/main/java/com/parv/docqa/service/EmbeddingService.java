package com.parv.docqa.service;

import com.parv.docqa.config.GeminiConfig;
import com.parv.docqa.service.GeminiService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;

    private static final String EMBEDDING_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    public EmbeddingService(RestTemplate restTemplate, GeminiConfig geminiConfig) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
    }

    public float[] generateEmbedding(String text) {

        // Build request body
        Map<String, Object> body = Map.of(
                "model", "models/gemini-embedding-001",
                "content", Map.of(
                        "parts", List.of(
                                Map.of("text", text)
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String urlWithKey = EMBEDDING_URL + "?key=" + geminiConfig.getApiKey();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                urlWithKey, entity, Map.class
        );

        // Extract embedding values from response
        Map embedding = (Map) response.getBody().get("embedding");
        List<Double> values = (List<Double>) embedding.get("values");

        // Convert List<Double> to float[]
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).floatValue();
        }

        return result;
    }

    // Convert float[] to pgvector string format
    // Example: [0.1, 0.2, 0.3]
    public String toVectorString(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            throw new IllegalArgumentException("Embedding is null or empty");
        }

        try {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < embedding.length; i++) {
                sb.append(embedding[i]);
                if (i < embedding.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            System.out.println("Error at index, embedding length: " + embedding.length);
            throw e;
        }
    }
}