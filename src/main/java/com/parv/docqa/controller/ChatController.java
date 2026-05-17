package com.parv.docqa.controller;

import com.parv.docqa.config.GeminiConfig;
import com.parv.docqa.dto.ChatMessage;
import com.parv.docqa.dto.ChatRequest;
import com.parv.docqa.dto.ResumeAnalysisResponse;
import com.parv.docqa.service.DocumentService;

import com.parv.docqa.service.GeminiService;
import com.parv.docqa.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
@Tag(name = "Chat", description = "General Q&A and document chat endpoints")
public class ChatController {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;
    private final DocumentService documentService;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;
    @Autowired
    GeminiService geminiService;

    public ChatController(RestTemplate restTemplate, GeminiConfig geminiConfig, DocumentService documentService, SessionService sessionService, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
        this.documentService = documentService;
        this.sessionService = sessionService;
        this.objectMapper=objectMapper;
    }
//    documentService.doSomething();

    @PostMapping("/chat")
    @Operation(summary = "Talking Tom", description = "Normal chat bot to chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {

        // Build request body for Gemini API
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", request.getQuestion())
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

        return ResponseEntity.ok(Map.of("answer", answer));

    }
    @Operation(summary = "Upload PDF and ask", description = "Upload any PDF document and ask a question about it")
    @PostMapping("/upload-and-ask")
    public ResponseEntity<Map<Object, Object>> uploadAndAsk(@RequestParam("file") MultipartFile file, @RequestParam("question") String question) throws Exception {
        System.out.println("The multipart" + file);
        // Step 1 - Extract text from PDF
        String documentText = documentService.extractFile(file);
        System.out.println(documentText);
        //  Step 2 - Build prompt with document context
        String prompt = "Here is a document:\n\n" + documentText +
                "\n\nBased on this document, answer the following question:\n" + question;
        String answer = geminiService.callGemini(prompt);
        System.out.println("the answer is" + answer);
        return ResponseEntity.ok().body(Map.of("answer", answer));
    }
    @Operation(summary = "Create session", description = "Create a new conversation session for multi-turn Q&A")
    @PostMapping("/session/create")
    public ResponseEntity<Map<String, String>> createSession() {
        String sessionId = sessionService.createSession();
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }
    @Operation(summary = "Chat with memory", description = "Ask questions with conversation history maintained per session")
    @PostMapping("/session/chat")// chat with session
    public ResponseEntity<Map<String, String>> sessionChat(@RequestParam("sessionId") String sessionId, @RequestBody ChatRequest request) {
        sessionService.addMessage(sessionId, new ChatMessage("user", request.getQuestion()));
        List<ChatMessage> history = sessionService.getChatHistory(sessionId);
        String answer = geminiService.callGemini2(request.getQuestion(), history);
        sessionService.addMessage(sessionId, new ChatMessage("model", answer));
        return ResponseEntity.ok(Map.of("sessionId", sessionId,
                "answer", answer
        ));
    }
    @Operation(summary = "Clear session", description = "Delete a conversation session and its history")
    @PostMapping("session/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        sessionService.removeSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session Cleared.....!"));
    }

    @PostMapping("sessionChat")
    @Operation(summary = "Chat History", description = "Give the chat history with to the session Id")
    public ResponseEntity<List<ChatMessage>> getSessionChatBySessionId(@RequestParam("sessionId") String sessionId) {
        return ResponseEntity.ok(sessionService.getChatHistory(sessionId));
    }
    @PostMapping("resume/analyze")
    @Operation(summary = "Analyze resume", description = "Upload a resume PDF and provide a job description to get a detailed skill-gap analysis")
    public ResponseEntity<?> analyze(@RequestParam("file") MultipartFile file,@RequestParam("request") String request) throws Exception{
          if(file.isEmpty()){
              throw new IllegalArgumentException("File Cannot be Empty....❌");
          }
          if (request.isEmpty() || request.isBlank()){
              throw new IllegalArgumentException("Job description cannot be blank");
          }
          String fileName=file.getOriginalFilename();
          if(fileName == null || !fileName.endsWith(".pdf")){
              throw new IllegalArgumentException("Only PDF file are supported");
          }
          String content = documentService.extractFile(file);
          String prompt = """
                You are an expert career coach and resume analyzer.
                
                Analyze the following resume against the job description and respond ONLY with a JSON object. No explanation, no markdown, just raw JSON.
                
                Resume:
                %s
                
                Job Description:
                %s
                
                Respond with exactly this JSON structure:
                {
                  "matchingSkills": ["skill1", "skill2"],
                  "missingSkills": ["skill1", "skill2"],
                  "matchScore": "75%%",
                  "summary": "Brief 2-3 sentence summary of the candidate fit",
                  "recommendations": ["recommendation1", "recommendation2"]
                }
                """.formatted(content, request);
          String response = geminiService.callGemini(prompt);
          ResumeAnalysisResponse result =
                  objectMapper.readValue(response, ResumeAnalysisResponse.class);
          return ResponseEntity.ok(result);

    }

}
