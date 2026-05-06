package com.investment.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String avatar;
    private String themePreference;
}
