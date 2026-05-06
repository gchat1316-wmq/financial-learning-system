package com.investment.service;

import com.investment.entity.Streak;
import com.investment.entity.UserProgress;
import com.investment.repository.StreakRepository;
import com.investment.repository.UserAnswerRepository;
import com.investment.repository.UserProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private UserProgressRepository userProgressRepository;

    @Mock
    private UserAnswerRepository userAnswerRepository;

    @Mock
    private StreakRepository streakRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private static final Long USER_ID = 1L;

    @Test
    void getDashboard_shouldReturnAllMetrics() {
        // Given
        when(userProgressRepository.countMasteredNodes(USER_ID)).thenReturn(5);

        List<Object[]> answerStats = new ArrayList<>();
        answerStats.add(new Object[]{100L, 80L});
        when(userAnswerRepository.countByUserId(USER_ID)).thenReturn(answerStats);

        when(userProgressRepository.findByUserId(USER_ID))
                .thenReturn(List.of(
                        createProgress(1L, 4),
                        createProgress(2L, 3),
                        createProgress(3L, 5)
                ));

        Streak streak = new Streak();
        streak.setCurrentStreak(7);
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(streak));

        // When
        Map<String, Object> dashboard = analyticsService.getDashboard(USER_ID);

        // Then
        assertEquals(5, dashboard.get("totalNodesMastered"));
        assertEquals(100, dashboard.get("totalQuestionsAnswered"));
        assertEquals(80.0, dashboard.get("accuracyRate"));
        assertEquals(7, dashboard.get("currentStreak"));
        assertEquals(3, dashboard.get("totalNodesStudied"));
        assertNotNull(dashboard.get("masteryByNode"));
    }

    @Test
    void getDashboard_noAnswers_returnsZeroAccuracy() {
        when(userProgressRepository.countMasteredNodes(USER_ID)).thenReturn(0);
        when(userAnswerRepository.countByUserId(USER_ID)).thenReturn(new ArrayList<>());
        when(userProgressRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Map<String, Object> dashboard = analyticsService.getDashboard(USER_ID);

        assertEquals(0.0, dashboard.get("accuracyRate"));
        assertEquals(0, dashboard.get("currentStreak"));
    }

    @Test
    void getWeeklyHeatmap_shouldReturnAll7Days() {
        // Given: data for 2 days
        List<Object[]> heatmapData = new ArrayList<>();
        heatmapData.add(new Object[]{2, 3});  // Tuesday: 3 sessions
        heatmapData.add(new Object[]{5, 5});  // Friday: 5 sessions
        when(userAnswerRepository.getHeatmapData(USER_ID)).thenReturn(heatmapData);

        // When
        Map<Integer, Integer> heatmap = analyticsService.getWeeklyHeatmap(USER_ID);

        // Then: all 7 days present, missing days = 0
        assertEquals(7, heatmap.size());
        assertEquals(0, heatmap.get(1));  // Monday
        assertEquals(3, heatmap.get(2));  // Tuesday
        assertEquals(0, heatmap.get(3));  // Wednesday
        assertEquals(0, heatmap.get(4));  // Thursday
        assertEquals(5, heatmap.get(5));  // Friday
        assertEquals(0, heatmap.get(6));  // Saturday
        assertEquals(0, heatmap.get(7));  // Sunday
    }

    @Test
    void getWeakTopics_shouldReturnLowestMasteryFirst() {
        // Given
        List<UserProgress> weakest = List.of(
                createProgress(10L, 0),
                createProgress(20L, 1),
                createProgress(30L, 2)
        );
        when(userProgressRepository.findWeakestNodes(USER_ID, 5)).thenReturn(weakest);

        // When
        List<Map<String, Object>> result = analyticsService.getWeakTopics(USER_ID, 5);

        // Then
        assertEquals(3, result.size());
        assertEquals(10L, result.get(0).get("nodeId"));
        assertEquals(0, result.get(0).get("masteryLevel"));
        assertEquals(20L, result.get(1).get("nodeId"));
        assertEquals(1, result.get(1).get("masteryLevel"));
    }

    @Test
    void getWeakTopics_noProgress_returnsEmptyList() {
        when(userProgressRepository.findWeakestNodes(USER_ID, 5)).thenReturn(List.of());

        List<Map<String, Object>> result = analyticsService.getWeakTopics(USER_ID, 5);

        assertTrue(result.isEmpty());
    }

    private UserProgress createProgress(Long nodeId, int masteryLevel) {
        UserProgress p = new UserProgress();
        p.setNodeId(nodeId);
        p.setMasteryLevel(masteryLevel);
        p.setReviewCount(1);
        p.setLastReviewedAt(LocalDateTime.now());
        return p;
    }
}
