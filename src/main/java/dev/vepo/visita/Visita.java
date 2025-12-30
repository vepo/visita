package dev.vepo.visita;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "visitas")
public class Visita extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String pagina;
    public String referer;
    public String userAgent;
    public String ip;
    public LocalDateTime dataAcesso;
    public LocalDateTime dataSaida;
    public Long duracao; // em segundos

    @RegisterForReflection
    public static record VisitaDiaria(String data, Long visitas, Long tempoMedio) {}
}