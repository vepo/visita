package dev.vepo.visita.tracking;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TrackingConfigResponse(java.util.List<String> ignoredPathPatterns) {}
