package dev.vepo.visita.shared.exception;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ErrorResponse(int status, String message) {}
