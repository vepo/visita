package dev.vepo.visita;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StartViewRequest(String language,
                                   @NotBlank String page,
                                   String referrer,
                                   String screenResolution,
                                   @NotBlank String tabId,
                                   @NotNull @Min(1) long timestamp,
                                   String timezone,
                                   String userAgent,
                                   @NotBlank String userId) {}