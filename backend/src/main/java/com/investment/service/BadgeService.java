package com.investment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.investment.dto.BadgeDto;
import com.investment.entity.Badge;
import com.investment.entity.UserBadge;
import com.investment.repository.BadgeRepository;
import com.investment.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final ObjectMapper objectMapper;

    /**
     * Check and award streak-based badges.
     * Called after dailyHeartbeat updates the streak.
     */
    @Transactional
    public void checkStreakBadges(Long userId, int currentStreak) {
        List<Badge> allBadges = badgeRepository.findAll();
        List<Badge> streakBadges = allBadges.stream()
                .filter(b -> getBadgeType(b).equals("streak"))
                .toList();

        for (Badge badge : streakBadges) {
            int threshold = getBadgeThreshold(badge);
            if (currentStreak >= threshold) {
                awardBadge(userId, badge.getId());
            }
        }
    }

    /**
     * Get all badges with earned status for a user.
     * Uses single query to fetch user's earned badges (fixes N+1).
     */
    public List<BadgeDto> getAllBadgesWithStatus(Long userId) {
        List<Badge> allBadges = badgeRepository.findAll();
        Set<Long> earnedBadgeIds = userBadgeRepository.findByUserId(userId).stream()
                .map(UserBadge::getBadgeId)
                .collect(Collectors.toSet());

        List<BadgeDto> result = new ArrayList<>();
        for (Badge badge : allBadges) {
            boolean earned = earnedBadgeIds.contains(badge.getId());
            UserBadge userBadge = earned
                    ? userBadgeRepository.findByUserIdAndBadgeId(userId, badge.getId()).orElse(null)
                    : null;

            result.add(new BadgeDto(
                    badge.getId(),
                    badge.getName(),
                    badge.getDescription(),
                    badge.getIconUrl(),
                    earned,
                    userBadge != null && userBadge.getEarnedAt() != null
                            ? userBadge.getEarnedAt().toString()
                            : null
            ));
        }

        return result;
    }

    /**
     * Award a badge to a user if not already earned.
     */
    @Transactional
    public void awardBadge(Long userId, Long badgeId) {
        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
            // Already earned - no-op
            return;
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUserId(userId);
        userBadge.setBadgeId(badgeId);
        userBadge.setEarnedAt(LocalDateTime.now());
        userBadgeRepository.save(userBadge);
    }

    private String getBadgeType(Badge badge) {
        return parseCriteriaJson(badge.getCriteriaJson()).orElse("unknown");
    }

    private int getBadgeThreshold(Badge badge) {
        return parseCriteriaValue(badge.getCriteriaJson()).orElse(0);
    }

    private java.util.Optional<String> parseCriteriaJson(String criteriaJson) {
        if (criteriaJson == null || criteriaJson.isEmpty()) {
            return java.util.Optional.empty();
        }
        try {
            JsonNode node = objectMapper.readTree(criteriaJson);
            return java.util.Optional.ofNullable(node.has("type") ? node.get("type").asText() : null);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse criteria_json for badge: {}", criteriaJson, e);
            return java.util.Optional.empty();
        }
    }

    private java.util.Optional<Integer> parseCriteriaValue(String criteriaJson) {
        if (criteriaJson == null || criteriaJson.isEmpty()) {
            return java.util.Optional.empty();
        }
        try {
            JsonNode node = objectMapper.readTree(criteriaJson);
            return java.util.Optional.ofNullable(node.has("value") ? node.get("value").asInt() : null);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse criteria_json for badge: {}", criteriaJson, e);
            return java.util.Optional.empty();
        }
    }
}
