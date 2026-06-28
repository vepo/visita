package dev.vepo.visita.domain.update;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record UpdateDomainRequest(@NotBlank String hostname, List<String> ignoredPathPatterns) {}
