package com.parv.docqa.repository;

import com.parv.docqa.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk,Long>{
    @Query(value ="SELECT id, document_name, content,\n" +
            "               1 - (embedding <=> CAST(:embedding AS vector)) AS similarity\n" +
            "        FROM document_chunks\n" +
            "        WHERE 1 - (embedding <=> CAST(:embedding AS vector)) > :threshold\n" +
            "        ORDER BY similarity DESC\n" +
            "        LIMIT :limit"
            , nativeQuery = true)
    List<Object []> findSimilarChunks(
            @Param("embedding") String embedding,
            @Param("limit") int limit,
            @Param("threshold") double threshold
    );

    // Check if document already exists
    boolean existsByDocumentName(String documentName);

    // Find chunks by document name
    List<DocumentChunk> findByDocumentName(String documentName);

    // Delete chunks by document name
    void deleteByDocumentName(String documentName);

}
