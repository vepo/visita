package dev.vepo.visita.domain.update;

import jakarta.validation.constraints.NotBlank;

public record UpdateDomainRequest(@NotBlank String hostname) { }
