package com.investment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProgressUpdateRequest {
    @NotNull(message = "quality is required")
    @Min(value = 0, message = "quality must be between 0 and 5")
    @Max(value = 5, message = "quality must be between 0 and 5")
    private Integer quality;
}
