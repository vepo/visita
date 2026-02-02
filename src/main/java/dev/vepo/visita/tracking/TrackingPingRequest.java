package dev.vepo.visita.tracking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TrackingPingRequest(@NotNull long id,
                                  @NotNull @Min(1) long timestamp) {}