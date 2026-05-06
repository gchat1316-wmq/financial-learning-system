package com.investment.controller;

import com.investment.entity.User;
import com.investment.repository.UserRepository;
import com.investment.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> dashboard = analyticsService.getDashboard(user.getId());
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/heatmap")
    public ResponseEntity<Map<Integer, Integer>> getHeatmap(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<Integer, Integer> heatmap = analyticsService.getWeeklyHeatmap(user.getId());
        return ResponseEntity.ok(heatmap);
    }

    @GetMapping("/weak-topics")
    public ResponseEntity<List<Map<String, Object>>> getWeakTopics(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Map<String, Object>> weakTopics = analyticsService.getWeakTopics(user.getId(), limit);
        return ResponseEntity.ok(weakTopics);
    }
}
