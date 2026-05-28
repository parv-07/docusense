package com.parv.docqa.service;

import com.parv.docqa.entity.DocumentChunk;
import com.parv.docqa.repository.DocumentChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    @Autowired
    DocumentService documentService;
    @Autowired
    EmbeddingService embeddingService;
    @Autowired
    DocumentChunkRepository chunkRepository;
    @Autowired
    GeminiService geminiService;

    public String storeDocument(MultipartFile multipartFile) throws Exception{
        String documentName= multipartFile.getOriginalFilename();
        String fullText = documentService.extractFile(multipartFile);
        List<String> chunks = splitIntoChunks(fullText, 500);

        for (String chunkText : chunks) {
            float[] embedding = embeddingService.generateEmbedding(chunkText);
            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocumentName(documentName);
            chunk.setContent(chunkText);
            chunk.setEmbedding(embedding);
            chunkRepository.save(chunk);
        }

        return "Stored " + chunks.size() + " chunks for " + documentName;
    }

    public String smartUploadAndQuery(MultipartFile file, String question) throws Exception {
        String documentName = file.getOriginalFilename();

        // Check if document already exists
        boolean alreadyStored = chunkRepository.existsByDocumentName(documentName);
        System.out.println("The already stored value"+alreadyStored);

        if (!alreadyStored) {
            // Store only if new document
            String fullText = documentService.extractFile(file);
            List<String> chunks = splitIntoChunks(fullText, 500);

            for (String chunkText : chunks) {
                float[] embedding = embeddingService.generateEmbedding(chunkText);
                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocumentName(documentName);
                chunk.setContent(chunkText);
                chunk.setEmbedding(embedding);
                chunkRepository.save(chunk);
            }
        }

        // Query regardless
        float[] questionEmbedding = embeddingService.generateEmbedding(question);
        String vectorString = embeddingService.toVectorString(questionEmbedding);
        try {
            List<Object []> similarChunks = chunkRepository
                    .findSimilarChunks(vectorString, 3,0.5);


            System.out.println("The value of vector string" + similarChunks);
            if (similarChunks == null && similarChunks.isEmpty()) {
                throw new RuntimeException("No relevant content found in the document. Please try a different question.");
            }
            similarChunks.forEach(row -> {
                System.out.println("Content: " + ((String) row[2]).substring(0, 50));
                System.out.println("Similarity: " + row[3]);
                System.out.println("---");
            });
            String context = similarChunks.stream()
                    .map(x-> (String) x[2])
                    .collect(Collectors.joining("\n\n"));

            return askGeminiWithContext(context, question);
        }catch (Exception ex){
            ex.printStackTrace();
            throw ex;
        }
    }

    private String askGeminiWithContext(String context, String question) {
        String prompt = """
                You are a helpful assistant. Answer the question ONLY using the context provided below.
                                        If the answer is not found in the context, say "I don't have information about that in this document."
                                        Do NOT use any outside knowledge.
                
                                        Context:
                                        %s
                
                                        Question: %s
                """.formatted(context, question);

        return geminiService.callGemini(prompt);
    }

    private List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += chunkSize;
        }
        return chunks;
    }
}
