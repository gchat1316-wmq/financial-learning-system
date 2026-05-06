package com.investment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investment.dto.BadgeDto;
import com.investment.entity.Badge;
import com.investment.entity.UserBadge;
import com.investment.repository.BadgeRepository;
import com.investment.repository.UserBadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private BadgeService badgeService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        badgeService = new BadgeService(badgeRepository, userBadgeRepository, objectMapper);
    }

    // === checkStreakBadges tests ===

    @Test
    void checkStreakBadges_streak3_shouldAward3DayBadge() {
        Badge badge = new Badge();
        badge.setId(2L);
        badge.setName("连续3天");
        badge.setCriteriaJson("{\"type\":\"streak\",\"value\":3}");

        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadgeId(USER_ID, 2L)).thenReturn(false);

        badgeService.checkStreakBadges(USER_ID, 3);

        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void checkStreakBadges_streak3_noOpIfAlreadyEarned() {
        Badge badge = new Badge();
        badge.setId(2L);
        badge.setCriteriaJson("{\"type\":\"streak\",\"value\":3}");

        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.existsByUserIdAndBadgeId(USER_ID, 2L)).thenReturn(true);

        badgeService.checkStreakBadges(USER_ID, 3);

        verify(userBadgeRepository, never()).save(any());
    }

    @Test
    void checkStreakBadges_streak2_shouldNotAward3DayBadge() {
        Badge badge = new Badge();
        badge.setId(2L);
        badge.setCriteriaJson("{\"type\":\"streak\",\"value\":3}");

        when(badgeRepository.findAll()).thenReturn(List.of(badge));

        badgeService.checkStreakBadges(USER_ID, 2);

        verify(userBadgeRepository, never()).save(any());
    }

    // === getAllBadgesWithStatus tests ===

    @Test
    void getAllBadgesWithStatus_shouldReturnAllBadgesWithCorrectEarnedFlag() {
        Badge badge1 = new Badge();
        badge1.setId(1L);
        badge1.setName("初学者");
        badge1.setCriteriaJson("{\"type\":\"node_count\",\"value\":1}");
        badge1.setIconUrl("/badges/beginner.png");

        Badge badge2 = new Badge();
        badge2.setId(2L);
        badge2.setName("连续3天");
        badge2.setCriteriaJson("{\"type\":\"streak\",\"value\":3}");
        badge2.setIconUrl("/badges/3day.png");

        UserBadge earnedBadge = new UserBadge();
        earnedBadge.setUserId(USER_ID);
        earnedBadge.setBadgeId(1L);
        earnedBadge.setEarnedAt(java.time.LocalDateTime.now().minusDays(1));

        when(badgeRepository.findAll()).thenReturn(Arrays.asList(badge1, badge2));
        when(userBadgeRepository.findByUserId(USER_ID)).thenReturn(List.of(earnedBadge));
        when(userBadgeRepository.findByUserIdAndBadgeId(USER_ID, 1L)).thenReturn(Optional.of(earnedBadge));

        List<BadgeDto> result = badgeService.getAllBadgesWithStatus(USER_ID);

        assertEquals(2, result.size());

        BadgeDto beginnerBadge = result.stream().filter(b -> b.getId().equals(1L)).findFirst().orElseThrow();
        assertTrue(beginnerBadge.getEarned());
        assertNotNull(beginnerBadge.getEarnedAt());

        BadgeDto streakBadge = result.stream().filter(b -> b.getId().equals(2L)).findFirst().orElseThrow();
        assertFalse(streakBadge.getEarned());
    }

    @Test
    void getAllBadgesWithStatus_noBadgesEarned_returnsAllUnearned() {
        Badge badge = new Badge();
        badge.setId(1L);
        badge.setName("初学者");
        badge.setCriteriaJson("{\"type\":\"node_count\",\"value\":1}");

        when(badgeRepository.findAll()).thenReturn(List.of(badge));
        when(userBadgeRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<BadgeDto> result = badgeService.getAllBadgesWithStatus(USER_ID);

        assertEquals(1, result.size());
        assertFalse(result.get(0).getEarned());
    }

    // === awardBadge tests ===

    @Test
    void awardBadge_newBadge_shouldSave() {
        when(userBadgeRepository.existsByUserIdAndBadgeId(USER_ID, 1L)).thenReturn(false);

        badgeService.awardBadge(USER_ID, 1L);

        verify(userBadgeRepository).save(any(UserBadge.class));
    }

    @Test
    void awardBadge_alreadyEarned_shouldNoOp() {
        when(userBadgeRepository.existsByUserIdAndBadgeId(USER_ID, 1L)).thenReturn(true);

        badgeService.awardBadge(USER_ID, 1L);

        verify(userBadgeRepository, never()).save(any());
    }
}
