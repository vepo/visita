package dev.vepo.visita;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record EstatisticaPorPagina(String pagina, Long visitas, Double tempoMedio, Long avg70, Long avg90) {
    public EstatisticaPorPagina(Object pagina, Object visitas, Object tempoMedio, Object avg70, Object avg90) {
        this((String) pagina, (Long) visitas, (Double) tempoMedio, (Long) avg70, (Long) avg90);
    }

}
