package com.investment.controller;

import com.investment.dto.StreakDto;
import com.investment.entity.Streak;
import com.investment.entity.User;
import com.investment.repository.UserRepository;
import com.investment.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<StreakDto> getStreak(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Streak streak = streakService.getOrInitializeStreak(user.getId());

        StreakDto dto = new StreakDto(
                streak.getUserId(),
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getLastStreakDate() != null ? streak.getLastStreakDate().toString() : null
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<StreakDto> heartbeat(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Streak streak = streakService.dailyHeartbeat(user.getId());

        StreakDto dto = new StreakDto(
                streak.getUserId(),
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getLastStreakDate() != null ? streak.getLastStreakDate().toString() : null
        );

        return ResponseEntity.ok(dto);
    }
}
