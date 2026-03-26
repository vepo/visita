package dev.vepo.visita.tracking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrackingStartRequest(String language,
                                   @NotBlank String page,
                                   String referrer,
                                   String screenResolution,
                                   @NotBlank String tabId,
                                   @NotNull @Min(1) long timestamp,
                                   String timezone,
                                   String userAgent,
                                   @NotBlank String userId) {}