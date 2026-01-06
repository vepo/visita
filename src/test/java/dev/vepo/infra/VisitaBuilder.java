package dev.vepo.infra;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import dev.vepo.visita.Visita;
import dev.vepo.visita.VisitaRepository;

public class VisitaBuilder {

    private String pagina;
    private Integer duracao;

    public VisitaBuilder() {
        pagina = null;
        duracao = null;
    }

    public VisitaBuilder withPagina(String pagina) {
        this.pagina = pagina;
        return this;
    }

    public VisitaBuilder withDuracao(int duracao) {
        this.duracao = duracao;
        return this;
    }

    public void persist() {
        var repo = Given.inject(VisitaRepository.class);
        var visita = new Visita(pagina, "test", "test", "test", System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(duracao));
        visita.setDataSaida(LocalDateTime.now());
        Given.withTransaction(() -> repo.save(visita));
    }

}
