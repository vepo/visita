package dev.vepo.visita;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record VisitaDiaria(String data, Long visitas, Long tempoMedio) {}