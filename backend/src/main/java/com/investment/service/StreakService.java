package com.investment.service;

import com.investment.entity.Streak;
import com.investment.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final StreakRepository streakRepository;
    private final BadgeService badgeService;

    /**
     * Process daily heartbeat for a user.
     * - Same day: no-op (idempotent)
     * - Yesterday: increment streak
     * - Gap > 1 day: reset streak to 1
     * - No existing record: create new with streak=1
     */
    @Transactional
    public Streak dailyHeartbeat(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayStart;

        Streak streak = streakRepository.findByUserId(userId)
                .orElseGet(() -> createNewStreak(userId));

        if (streak.getLastStreakDate() == null) {
            // First time ever - start streak
            streak.setCurrentStreak(1);
            streak.setLastStreakDate(now);
            updateLongestStreak(streak);
        } else {
            LocalDateTime lastDate = streak.getLastStreakDate();
            LocalDateTime lastDateStart = lastDate.toLocalDate().atStartOfDay();

            if (lastDateStart.equals(todayStart)) {
                // Already checked in today - no-op (idempotent)
                return streak;
            } else if (lastDateStart.equals(yesterdayStart)) {
                // Consecutive day - increment
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
                streak.setLastStreakDate(now);
                updateLongestStreak(streak);
            } else {
                // Gap in streak - reset
                streak.setCurrentStreak(1);
                streak.setLastStreakDate(now);
                // longest_streak stays as is (record of achievement)
            }
        }

        Streak saved = streakRepository.save(streak);

        // Check for badge awards
        badgeService.checkStreakBadges(userId, saved.getCurrentStreak());

        return saved;
    }

    /**
     * Get streak record for a user.
     */
    public Optional<Streak> getStreak(Long userId) {
        return streakRepository.findByUserId(userId);
    }

    /**
     * Get or initialize streak for a user.
     */
    public Streak getOrInitializeStreak(Long userId) {
        return streakRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Streak s = new Streak();
                    s.setUserId(userId);
                    s.setCurrentStreak(0);
                    s.setLongestStreak(0);
                    return streakRepository.save(s);
                });
    }

    private Streak createNewStreak(Long userId) {
        Streak streak = new Streak();
        streak.setUserId(userId);
        streak.setCurrentStreak(0);
        streak.setLongestStreak(0);
        return streak;
    }

    private void updateLongestStreak(Streak streak) {
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
    }
}
