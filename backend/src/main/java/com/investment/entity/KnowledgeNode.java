package com.investment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "knowledge_nodes")
public class KnowledgeNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private String title;

    @Column(name = "content_json", columnDefinition = "JSON")
    private String contentJson;

    @Column(name = "infographic_url")
    private String infographicUrl;

    private Integer difficulty = 1;

    @Column(name = "node_type")
    private String nodeType = "point";

    @Column(name = "prerequisite_ids", columnDefinition = "JSON")
    private String prerequisiteIds;

    @Column(name = "related_node_ids", columnDefinition = "JSON")
    private String relatedNodeIds;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
