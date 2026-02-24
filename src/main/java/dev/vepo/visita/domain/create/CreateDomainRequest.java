package dev.vepo.visita.domain.create;

import jakarta.validation.constraints.NotBlank;

public record CreateDomainRequest(@NotBlank String hostname) {}
