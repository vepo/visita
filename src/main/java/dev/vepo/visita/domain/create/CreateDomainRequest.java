package dev.vepo.visita.domain.create;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record CreateDomainRequest(@NotBlank String hostname, List<String> ignoredPathPatterns) {}
