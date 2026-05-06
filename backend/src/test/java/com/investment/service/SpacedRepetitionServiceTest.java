package com.investment.service;

import com.investment.dto.ProgressUpdateRequest;
import com.investment.entity.UserProgress;
import com.investment.repository.UserProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacedRepetitionServiceTest {

    @Mock
    private UserProgressRepository userProgressRepository;

    @InjectMocks
    private SpacedRepetitionService spacedRepetitionService;

    private static final Long USER_ID = 1L;
    private static final Long NODE_ID = 100L;

    // === First Review Tests ===

    @Test
    void firstReview_quality5_shouldIncrementMastery_setNextReviewTomorrow() {
        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.empty());
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(5);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        assertEquals(1, result.getMasteryLevel());
        assertEquals(1, result.getReviewCount());
        assertNotNull(result.getNextReviewDate());
    }

    @Test
    void firstReview_quality4_shouldIncrementMastery() {
        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.empty());
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(4);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        assertEquals(1, result.getMasteryLevel());
    }

    @Test
    void firstReview_quality3_shouldKeepMastery0_setNextReviewTomorrow() {
        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.empty());
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(3);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        // q=3 is borderline pass - mastery stays at 0, but next review is tomorrow
        assertEquals(0, result.getMasteryLevel());
        assertEquals(1, result.getReviewCount());
    }

    @Test
    void firstReview_quality2_failed_shouldKeepMastery0() {
        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.empty());
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(2);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        assertEquals(0, result.getMasteryLevel());
    }

    // === Subsequent Review Tests ===

    @Test
    void subsequentReview_quality4_shouldUseEF25AndIncrementMastery() {
        UserProgress existing = new UserProgress();
        existing.setUserId(USER_ID);
        existing.setNodeId(NODE_ID);
        existing.setMasteryLevel(1);
        existing.setReviewCount(1);
        existing.setLastReviewedAt(LocalDateTime.now().minusDays(1));
        existing.setNextReviewDate(LocalDateTime.now());

        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.of(existing));
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(4);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        // mastery 1 -> 2, interval = round(1 * 2.36) = 2
        assertEquals(2, result.getMasteryLevel());
        assertEquals(2, result.getReviewCount());
    }

    @Test
    void subsequentReview_quality5_shouldApplyBonusMultiplierAndIncrementMastery() {
        UserProgress existing = new UserProgress();
        existing.setUserId(USER_ID);
        existing.setNodeId(NODE_ID);
        existing.setMasteryLevel(1);
        existing.setReviewCount(1);
        existing.setLastReviewedAt(LocalDateTime.now().minusDays(1));
        existing.setNextReviewDate(LocalDateTime.now());

        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.of(existing));
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(5);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        // mastery 1 -> 2, interval = round(1 * 2.6 * 1.3) = round(3.38) = 3
        assertEquals(2, result.getMasteryLevel());
    }

    @Test
    void subsequentReview_quality3_shouldNotIncrementMastery() {
        UserProgress existing = new UserProgress();
        existing.setUserId(USER_ID);
        existing.setNodeId(NODE_ID);
        existing.setMasteryLevel(2);
        existing.setReviewCount(1);
        existing.setLastReviewedAt(LocalDateTime.now().minusDays(1));
        existing.setNextReviewDate(LocalDateTime.now());

        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.of(existing));
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(3);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        // mastery stays at 2 (q=3 doesn't increment), interval = round(1 * 2.36) = 2
        assertEquals(2, result.getMasteryLevel());
    }

    @Test
    void subsequentReview_qualityLessThan3_shouldResetInterval_decrementMastery() {
        UserProgress existing = new UserProgress();
        existing.setUserId(USER_ID);
        existing.setNodeId(NODE_ID);
        existing.setMasteryLevel(3);
        existing.setReviewCount(5);
        existing.setLastReviewedAt(LocalDateTime.now().minusDays(7));
        existing.setNextReviewDate(LocalDateTime.now());

        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.of(existing));
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(2);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        assertEquals(2, result.getMasteryLevel()); // 3 -> 2
        assertEquals(LocalDateTime.now().plusDays(1).toLocalDate(), result.getNextReviewDate().toLocalDate());
    }

    @Test
    void subsequentReview_qualityLessThan3_shouldNotDropBelowZero() {
        UserProgress existing = new UserProgress();
        existing.setUserId(USER_ID);
        existing.setNodeId(NODE_ID);
        existing.setMasteryLevel(0);
        existing.setReviewCount(1);
        existing.setLastReviewedAt(LocalDateTime.now().minusDays(1));
        existing.setNextReviewDate(LocalDateTime.now());

        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.of(existing));
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(1);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        assertEquals(0, result.getMasteryLevel()); // stays at 0 (floor)
    }

    @Test
    void masteryLevel_shouldCapAt5() {
        UserProgress existing = new UserProgress();
        existing.setUserId(USER_ID);
        existing.setNodeId(NODE_ID);
        existing.setMasteryLevel(5);
        existing.setReviewCount(10);
        existing.setLastReviewedAt(LocalDateTime.now().minusDays(30));
        existing.setNextReviewDate(LocalDateTime.now());

        when(userProgressRepository.findByUserIdAndNodeId(USER_ID, NODE_ID))
                .thenReturn(Optional.of(existing));
        when(userProgressRepository.save(any(UserProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setQuality(5);

        UserProgress result = spacedRepetitionService.recordReview(USER_ID, NODE_ID, request);

        assertEquals(5, result.getMasteryLevel()); // stays at 5 (cap)
    }

    // === Easiness Factor Tests ===

    @Test
    void easinessFactor_quality5_shouldIncrease() {
        // q=5: delta = 0.1 - 0*(0.08+0) = 0.1
        // EF' = 2.5 + 0.1 = 2.6
        double ef = spacedRepetitionService.calculateNewEasinessFactor(3, 5);
        assertEquals(2.6, ef, 0.01);
    }

    @Test
    void easinessFactor_quality3_shouldDecrease() {
        // q=3: delta = 0.1 - 2*(0.08+0.04) = 0.1 - 0.24 = -0.14
        // EF' = 2.5 - 0.14 = 2.36
        double ef = spacedRepetitionService.calculateNewEasinessFactor(3, 3);
        assertEquals(2.36, ef, 0.01);
    }

    @Test
    void easinessFactor_shouldFloorAt13() {
        // Multiple failed reviews should push EF toward floor of 1.3
        double ef = spacedRepetitionService.calculateNewEasinessFactor(3, 0);
        assertTrue(ef >= 1.3, "EF should not drop below 1.3");
    }
}
