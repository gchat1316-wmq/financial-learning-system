package com.investment.service;

import com.investment.dto.ProgressUpdateRequest;
import com.investment.entity.UserProgress;
import com.investment.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * SM-2 Spaced Repetition Algorithm Implementation.
 *
 * Algorithm parameters:
 * - EF (Easiness Factor): initial = 2.5, minimum = 1.3
 * - Quality grades: 0 (complete blackout) to 5 (perfect response)
 * - First review: interval = 1 day for all outcomes; mastery increments only on q >= 4
 * - Subsequent reviews:
 *   - q < 3:  interval = 1, mastery = max(0, mastery - 1)
 *   - q == 3: interval = max(1, round(interval * EF)), mastery unchanged
 *   - q == 4: interval = round(interval * EF), mastery += 1 (capped at 5)
 *   - q == 5: interval = round(interval * EF * 1.3), mastery += 1 (capped at 5)
 * - mastery_level: 0-5, min floor 0, max ceiling 5
 * - next_review_date = today + interval
 */
@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private static final double INITIAL_EASINESS_FACTOR = 2.5;
    private static final double MIN_EASINESS_FACTOR = 1.3;
    private static final double Q_MODIFIER_EASY = 1.3;

    private final UserProgressRepository userProgressRepository;

    @Transactional
    public UserProgress recordReview(Long userId, Long nodeId, ProgressUpdateRequest request) {
        int quality = request.getQuality();
        UserProgress progress = userProgressRepository.findByUserIdAndNodeId(userId, nodeId)
                .orElseGet(() -> createNewProgress(userId, nodeId));

        if (progress.getReviewCount() == 0) {
            // First review: always schedule for tomorrow, mastery increments only on strong pass
            progress.setNextReviewDate(LocalDateTime.now().plusDays(1));
            if (quality >= 4) {
                progress.setMasteryLevel(Math.min(5, progress.getMasteryLevel() + 1));
            }
        } else {
            // Subsequent reviews
            if (quality < 3) {
                // Failed - reset interval, decrease mastery
                progress.setMasteryLevel(Math.max(0, progress.getMasteryLevel() - 1));
                progress.setNextReviewDate(LocalDateTime.now().plusDays(1));
            } else {
                // Passed
                int currentInterval = calculateCurrentInterval(progress);
                double ef = calculateNewEasinessFactor(progress.getMasteryLevel(), quality);

                int newInterval;
                if (quality == 3) {
                    // Correct but difficult - conservative interval
                    newInterval = Math.max(1, (int) Math.round(currentInterval * ef));
                } else if (quality == 4) {
                    // Correct
                    newInterval = (int) Math.round(currentInterval * ef);
                    progress.setMasteryLevel(Math.min(5, progress.getMasteryLevel() + 1));
                } else { // quality == 5
                    // Perfect
                    newInterval = (int) Math.round(currentInterval * ef * Q_MODIFIER_EASY);
                    progress.setMasteryLevel(Math.min(5, progress.getMasteryLevel() + 1));
                }
                progress.setNextReviewDate(LocalDateTime.now().plusDays(newInterval));
            }
        }

        progress.setReviewCount(progress.getReviewCount() + 1);
        progress.setLastReviewedAt(LocalDateTime.now());
        return userProgressRepository.save(progress);
    }

    public Optional<UserProgress> getProgress(Long userId, Long nodeId) {
        return userProgressRepository.findByUserIdAndNodeId(userId, nodeId);
    }

    public java.util.List<UserProgress> getDueReviews(Long userId) {
        return userProgressRepository.findByUserIdAndNextReviewDateBefore(userId, LocalDateTime.now().plusDays(1));
    }

    private UserProgress createNewProgress(Long userId, Long nodeId) {
        UserProgress progress = new UserProgress();
        progress.setUserId(userId);
        progress.setNodeId(nodeId);
        progress.setMasteryLevel(0);
        progress.setReviewCount(0);
        progress.setNextReviewDate(LocalDateTime.now());
        return progress;
    }

    double calculateNewEasinessFactor(int currentMastery, int quality) {
        double delta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02);
        return Math.max(MIN_EASINESS_FACTOR, INITIAL_EASINESS_FACTOR + delta);
    }

    private int calculateCurrentInterval(UserProgress progress) {
        if (progress.getLastReviewedAt() == null) {
            return 1;
        }
        long days = ChronoUnit.DAYS.between(progress.getLastReviewedAt(), LocalDateTime.now());
        return Math.max(1, (int) days);
    }
}
