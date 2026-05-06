package com.investment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String author;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "chapters_json", columnDefinition = "JSON")
    private String chaptersJson;

    @Column(name = "linked_node_ids", columnDefinition = "JSON")
    private String linkedNodeIds;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
