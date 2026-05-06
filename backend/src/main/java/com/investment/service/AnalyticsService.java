package com.investment.service;

import com.investment.entity.UserProgress;
import com.investment.repository.UserAnswerRepository;
import com.investment.repository.UserProgressRepository;
import com.investment.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserProgressRepository userProgressRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final StreakRepository streakRepository;

    /**
     * Dashboard data for the analytics screen.
     * Returns: total_nodes_mastered, total_questions_answered, accuracy_rate, current_streak, mastery_by_topic
     */
    public Map<String, Object> getDashboard(Long userId) {
        // Total mastered nodes (mastery >= 4)
        int totalMastered = userProgressRepository.countMasteredNodes(userId);

        // Total questions answered and accuracy
        int totalAnswered = 0;
        int totalCorrect = 0;
        List<Object[]> answerStats = userAnswerRepository.countByUserId(userId);
        if (!answerStats.isEmpty()) {
            Object[] stats = answerStats.get(0);
            totalAnswered = ((Number) stats[0]).intValue();
            totalCorrect = ((Number) stats[1]).intValue();
        }
        double accuracyRate = totalAnswered > 0 ? (double) totalCorrect / totalAnswered * 100 : 0.0;

        // Current streak
        int currentStreak = streakRepository.findByUserId(userId)
                .map(s -> s.getCurrentStreak())
                .orElse(0);

        // Mastery by topic (for radar chart) - group by node_id
        List<UserProgress> allProgress = userProgressRepository.findByUserId(userId);
        Map<Long, Integer> masteryByNode = allProgress.stream()
                .collect(Collectors.toMap(
                        UserProgress::getNodeId,
                        UserProgress::getMasteryLevel,
                        (a, b) -> a
                ));

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalNodesMastered", totalMastered);
        dashboard.put("totalQuestionsAnswered", totalAnswered);
        dashboard.put("accuracyRate", Math.round(accuracyRate * 100.0) / 100.0);
        dashboard.put("currentStreak", currentStreak);
        dashboard.put("masteryByNode", masteryByNode);
        dashboard.put("totalNodesStudied", allProgress.size());

        return dashboard;
    }

    /**
     * Weekly heatmap data.
     * Returns map of day-of-week (1=Monday to 7=Sunday) -> count of study sessions.
     */
    public Map<Integer, Integer> getWeeklyHeatmap(Long userId) {
        List<Object[]> results = userAnswerRepository.getHeatmapData(userId);

        // Initialize all days to 0
        Map<Integer, Integer> heatmap = new LinkedHashMap<>();
        for (int i = 1; i <= 7; i++) {
            heatmap.put(i, 0);
        }

        for (Object[] row : results) {
            int dayOfWeek = ((Number) row[0]).intValue();
            int count = ((Number) row[1]).intValue();
            heatmap.put(dayOfWeek, count);
        }

        return heatmap;
    }

    /**
     * Get weakest topics (lowest mastery level).
     */
    public List<Map<String, Object>> getWeakTopics(Long userId, int limit) {
        List<UserProgress> weakest = userProgressRepository.findWeakestNodes(userId, limit);

        return weakest.stream()
                .map(p -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("nodeId", p.getNodeId());
                    item.put("masteryLevel", p.getMasteryLevel());
                    item.put("reviewCount", p.getReviewCount());
                    item.put("lastReviewedAt", p.getLastReviewedAt());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
