package com.investment.service;

import com.investment.entity.Streak;
import com.investment.repository.StreakRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StreakServiceTest {

    @Mock
    private StreakRepository streakRepository;

    private BadgeService badgeService;
    private StreakService streakService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Create real BadgeService with real ObjectMapper (can't mock due to @Slf4j)
        badgeService = new BadgeService(
                mock(com.investment.repository.BadgeRepository.class),
                mock(com.investment.repository.UserBadgeRepository.class),
                new ObjectMapper()
        );
        streakService = new StreakService(streakRepository, badgeService);
    }

    @Test
    void dailyHeartbeat_firstTimeUser_createsStreakWith1() {
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(streakRepository.save(any(Streak.class)))
                .thenAnswer(inv -> {
                    Streak s = inv.getArgument(0);
                    s.setId(1L);
                    return s;
                });

        Streak result = streakService.dailyHeartbeat(USER_ID);

        assertEquals(1, result.getCurrentStreak());
        assertEquals(1, result.getLongestStreak());
        assertNotNull(result.getLastStreakDate());
    }

    @Test
    void dailyHeartbeat_sameDay_twice_isIdempotent() {
        LocalDateTime today = LocalDateTime.now();
        Streak existing = new Streak();
        existing.setId(1L);
        existing.setUserId(USER_ID);
        existing.setCurrentStreak(5);
        existing.setLongestStreak(5);
        existing.setLastStreakDate(today);

        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        Streak result = streakService.dailyHeartbeat(USER_ID);

        assertEquals(5, result.getCurrentStreak());
        // badgeService.checkStreakBadges should not be called (same day)
        verify(streakRepository, never()).save(any());
    }

    @Test
    void dailyHeartbeat_consecutiveDay_incrementsStreak() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        Streak existing = new Streak();
        existing.setId(1L);
        existing.setUserId(USER_ID);
        existing.setCurrentStreak(5);
        existing.setLongestStreak(5);
        existing.setLastStreakDate(yesterday);

        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
        when(streakRepository.save(any(Streak.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Streak result = streakService.dailyHeartbeat(USER_ID);

        assertEquals(6, result.getCurrentStreak());
        assertEquals(6, result.getLongestStreak()); // beat previous record
    }

    @Test
    void dailyHeartbeat_missedDay_resetsStreakTo1() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        Streak existing = new Streak();
        existing.setId(1L);
        existing.setUserId(USER_ID);
        existing.setCurrentStreak(10);
        existing.setLongestStreak(10);
        existing.setLastStreakDate(threeDaysAgo);

        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
        when(streakRepository.save(any(Streak.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Streak result = streakService.dailyHeartbeat(USER_ID);

        assertEquals(1, result.getCurrentStreak());
        assertEquals(10, result.getLongestStreak()); // record preserved
    }

    @Test
    void getOrInitializeStreak_existing_returnsExisting() {
        Streak existing = new Streak();
        existing.setId(1L);
        existing.setUserId(USER_ID);
        existing.setCurrentStreak(7);
        existing.setLongestStreak(7);

        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        Streak result = streakService.getOrInitializeStreak(USER_ID);

        assertEquals(7, result.getCurrentStreak());
        verify(streakRepository, never()).save(any());
    }

    @Test
    void getOrInitializeStreak_noExisting_createsNew() {
        when(streakRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(streakRepository.save(any(Streak.class)))
                .thenAnswer(inv -> {
                    Streak s = inv.getArgument(0);
                    s.setId(1L);
                    return s;
                });

        Streak result = streakService.getOrInitializeStreak(USER_ID);

        assertEquals(0, result.getCurrentStreak());
        assertEquals(0, result.getLongestStreak());
        verify(streakRepository).save(any(Streak.class));
    }
}
