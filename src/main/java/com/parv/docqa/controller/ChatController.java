package com.parv.docqa.controller;

import com.parv.docqa.config.GeminiConfig;
import com.parv.docqa.dto.ChatMessage;
import com.parv.docqa.dto.ChatRequest;
import com.parv.docqa.service.DocumentService;

import com.parv.docqa.service.SessionService;
import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
public class ChatController {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;
    private final DocumentService documentService;
    private final SessionService sessionService;
   public ChatController(RestTemplate restTemplate, GeminiConfig geminiConfig, DocumentService documentService , SessionService sessionService){
       this.restTemplate = restTemplate;
       this.geminiConfig=geminiConfig;
       this.documentService=documentService;
       this.sessionService=sessionService;
   }

    @PostMapping("/chat")
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

    @PostMapping("/upload-and-ask")
    public ResponseEntity<Map<Object,Object>> uploadAndAsk(@RequestParam("file") MultipartFile file, @RequestParam("question") String question) throws Exception {
        System.out.println("The multipart"+file);
        // Step 1 - Extract text from PDF
        String documentText = documentService.extractFile(file);
        System.out.println(documentText);
       //  Step 2 - Build prompt with document context
        String prompt = "Here is a document:\n\n" + documentText +
                "\n\nBased on this document, answer the following question:\n" + question;
         String answer= callGemini(prompt);
System.out.println("the answer is"+answer);
return ResponseEntity.ok().body(Map.of("answer",answer));
    }

    private String callGemini(String prompt) {

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
    @PostMapping("/session/create")
    public ResponseEntity<Map<String,String>> createSession(){
       String sessionId= sessionService.createSession();
       return ResponseEntity.ok(Map.of("sessionId",sessionId));
    }
   @PostMapping("/session/chat")// chat with session
    public ResponseEntity<Map<String,String>> sessionChat(@RequestParam("sessionId") String sessionId, @RequestBody ChatRequest request){
       sessionService.addMessage(sessionId, new ChatMessage("user", request.getQuestion()));

List<ChatMessage> history =sessionService.getChatHistory(sessionId);
String answer = callGemini2(request.getQuestion(),history);
sessionService.addMessage(sessionId,new ChatMessage("model",answer));
return ResponseEntity.ok(Map.of("sessionId",sessionId,
       "answer",answer
       ));
   }

    private String callGemini2(String question, List<ChatMessage> history) {
       List<Map<String,Object>> contents;
       if(history!=null && history.size() >1){
           contents=history.stream().map(msg-> Map.<String, Object>of(
                   "role", msg.getRole(),
                   "parts", List.of(Map.of("text", msg.getContent()))
           )).collect(Collectors.toList());
       }else{
           contents=List.of(Map.<String, Object>of("role",history.get(0).getRole(),"parts",List.of(Map.of("text",history.get(0).getContent()))));
       }
       Map<String,Object> body = Map.of("contents",contents);
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
    @PostMapping("session/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId){
    sessionService.removeSession(sessionId);
    return ResponseEntity.ok(Map.of("message","Session Cleared.....!"));
    }
    @PostMapping("sessionChat")
    public ResponseEntity<List<ChatMessage>> getSessionChatBySessionId(@RequestParam("sessionId") String sessionId){
       return ResponseEntity.ok( sessionService.getChatHistory(sessionId));
    }

}
