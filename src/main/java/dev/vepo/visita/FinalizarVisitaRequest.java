package dev.vepo.visita;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FinalizarVisitaRequest(@NotNull long id,
                                     @NotNull @Min(1) long timestamp) {}