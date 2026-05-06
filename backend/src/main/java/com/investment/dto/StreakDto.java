package com.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StreakDto {
    private Long userId;
    private Integer currentStreak;
    private Integer longestStreak;
    private String lastStreakDate;
}
