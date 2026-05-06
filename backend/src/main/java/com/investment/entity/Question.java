package com.investment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Column(name = "question_type")
    private String questionType = "choice";

    @Column(name = "content_json", columnDefinition = "JSON", nullable = false)
    private String contentJson;

    @Column(name = "answer_json", columnDefinition = "JSON", nullable = false)
    private String answerJson;

    private String explanation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
