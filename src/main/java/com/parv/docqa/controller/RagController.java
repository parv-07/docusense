package com.parv.docqa.controller;

import com.parv.docqa.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@Tag(name = "RAG", description = "Retrieval Augmented Generation endpoints")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    // Store document only
    @Operation(summary = "Store document",
            description = "Upload PDF to store in vector database")
    @PostMapping("/store")
    public ResponseEntity<Map<String, String>> storeDocument(
            @RequestParam("file") MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String result = ragService.storeDocument(file);
        return ResponseEntity.ok(Map.of("message", result));
    }

    // Query already stored documents
//    @Operation(summary = "Query document",
//            description = "Ask question about already stored documents")
//    @PostMapping("/query")
//    public ResponseEntity<Map<String, String>> queryDocument(
//            @RequestBody Map<String, String> request) {
//
//        if (request.get("question") == null || request.get("question").isBlank()) {
//            throw new IllegalArgumentException("Question cannot be empty");
//        }
//
////        String answer = ragService.queryDocument(request.get("question"));
//        return ResponseEntity.ok(Map.of("answer", answer));
//    }

    // Smart ask — store only if new, then query
    @Operation(summary = "Smart upload and ask",
            description = "Stores document only if not already stored, then queries using RAG")
    @PostMapping("/smart-ask")
    public ResponseEntity<Map<String, String>> smartAsk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("question") String question) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }

        String answer = ragService.smartUploadAndQuery(file, question);
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    // Re-upload — force refresh document
//    @Operation(summary = "Re-upload document",
//            description = "Force re-upload — deletes old chunks and stores fresh")
//    @PostMapping("/reupload")
//    public ResponseEntity<Map<String, String>> reUpload(
//            @RequestParam("file") MultipartFile file) throws Exception {
//
//        if (file.isEmpty()) {
//            throw new IllegalArgumentException("File cannot be empty");
//        }
//
//        String result = ragService.reUploadDocument(file);
//        return ResponseEntity.ok(Map.of("message", "Re-uploaded: " + result));
//    }
}