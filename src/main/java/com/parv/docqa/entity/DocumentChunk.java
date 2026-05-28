package com.parv.docqa.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "document_chunks")

public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "embedding", columnDefinition = "vector(3072)")
    private float[] embedding;
}