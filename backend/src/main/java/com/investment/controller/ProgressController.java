package com.investment.controller;

import com.investment.dto.ProgressUpdateRequest;
import com.investment.entity.User;
import com.investment.entity.UserProgress;
import com.investment.repository.UserRepository;
import com.investment.service.SpacedRepetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class ProgressController {

    private final SpacedRepetitionService spacedRepetitionService;
    private final UserRepository userRepository;

    @PostMapping("/{nodeId}/progress")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @PathVariable Long nodeId,
            @Valid @RequestBody ProgressUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserProgress progress = spacedRepetitionService.recordReview(user.getId(), nodeId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("nodeId", progress.getNodeId());
        response.put("masteryLevel", progress.getMasteryLevel());
        response.put("nextReviewDate", progress.getNextReviewDate());
        response.put("reviewCount", progress.getReviewCount());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{nodeId}/progress")
    public ResponseEntity<Map<String, Object>> getProgress(
            @PathVariable Long nodeId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return spacedRepetitionService.getProgress(user.getId(), nodeId)
                .map(progress -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("nodeId", progress.getNodeId());
                    response.put("masteryLevel", progress.getMasteryLevel());
                    response.put("nextReviewDate", progress.getNextReviewDate());
                    response.put("reviewCount", progress.getReviewCount());
                    response.put("lastReviewedAt", progress.getLastReviewedAt());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
