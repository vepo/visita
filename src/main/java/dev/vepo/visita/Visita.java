package dev.vepo.visita;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_views")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page")
    private String pagina;

    private String referrer;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "access_timestamp")
    private LocalDateTime dataAcesso;

    @Column(name = "end_timestamp")
    private LocalDateTime dataSaida;

    @Column(name = "length")
    private Long duracao; // em segundos

    private String timezone;

    public Visita() {}

    public Visita(String page, String referrer, String userAgent, String timezone, long timestamp) {
        this.pagina = page;
        this.referrer = referrer;
        this.timezone = timezone;
        this.userAgent = userAgent;
        this.dataAcesso = Instant.ofEpochMilli(timestamp)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDateTime();
    }

    public Visita(String page, long timestamp, Visita extended) {
        this.pagina = page;
        this.referrer = extended.referrer;
        this.userAgent = extended.userAgent;
        this.timezone = extended.timezone;
        this.dataAcesso = Instant.ofEpochMilli(timestamp)
                                 .atZone(ZoneId.systemDefault())
                                 .toLocalDateTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPagina() {
        return pagina;
    }

    public void setPagina(String pagina) {
        this.pagina = pagina;
    }

    public boolean isSamePage(String page) {
        return Objects.equals(pagina, page);
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public LocalDateTime getDataAcesso() {
        return dataAcesso;
    }

    public void setDataAcesso(LocalDateTime dataAcesso) {
        this.dataAcesso = dataAcesso;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDateTime dataSaida) {
        this.dataSaida = dataSaida;
        if (this.dataAcesso != null && this.dataSaida != null) {
            this.duracao = ChronoUnit.SECONDS.between(this.dataAcesso, this.dataSaida);
        }
    }

    public Long getDuracao() {
        return duracao;
    }

    public void setDuracao(Long duracao) {
        this.duracao = duracao;
    }

    public void extendDuration(long timestamp) {
        this.duracao = ChronoUnit.SECONDS.between(this.dataAcesso, Instant.ofEpochMilli(timestamp)
                                                                          .atZone(ZoneId.systemDefault())
                                                                          .toLocalDateTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        } else {
            return Objects.equals(((Visita) obj).id, id);
        }
    }

    @Override
    public String toString() {
        return "Visita [id=%s, pagina=%s, referrer=%s, userAgent=%s, timezone=%s, dataAcesso=%s, dataSaida=%s, duracao=%s]".formatted(id, pagina, referrer,
                                                                                                                                     userAgent,
                                                                                                                                     timezone, dataAcesso,
                                                                                                                                     dataSaida,
                                                                                                                                     duracao);
    }
}