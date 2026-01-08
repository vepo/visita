package dev.vepo.visita;

import java.time.LocalDate;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record EstatisticaPorDia(LocalDate data, Long visitas, Double tempoMedio, Long avg70, Long avg90) {
    public EstatisticaPorDia(Object data, Object visitas, Object tempoMedio, Object avg70, Object avg90) {
        this((LocalDate) data, (Long) visitas, (Double) tempoMedio, (Long) avg70, (Long) avg90);
    }
}
