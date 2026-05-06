package com.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BadgeDto {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private Boolean earned;
    private String earnedAt;
}
