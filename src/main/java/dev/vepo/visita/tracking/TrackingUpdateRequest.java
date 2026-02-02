package dev.vepo.visita.tracking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrackingUpdateRequest(@NotNull long id,
                                    @NotBlank String page,
                                    @NotBlank String tabId,
                                    @NotNull @Min(1) long timestamp,
                                    @NotBlank String userId) {}