package dev.vepo.visita;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "visitas")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pagina;
    private String referer;
    private String userAgent;
    private String ip;
    private LocalDateTime dataAcesso;
    private LocalDateTime dataSaida;

    public Long duracao; // em segundos

    public Visita() {}

    public Visita(String page, String referer, String userAgent, String ip) {
        this.pagina = page;
        this.referer = referer;
        this.userAgent = userAgent;
        this.ip = ip;
        this.dataAcesso = LocalDateTime.now();
    }

    public Visita(String page, Visita extended) {
        this.pagina = page;
        this.referer = extended.referer;
        this.userAgent = extended.userAgent;
        this.ip = extended.ip;
        this.dataAcesso = LocalDateTime.now();
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

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public void extendDuration() {
        this.duracao = ChronoUnit.SECONDS.between(this.dataAcesso, LocalDateTime.now());
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
        return "Visita [id=%s, pagina=%s, referer=%s, userAgent=%s, ip=%s, dataAcesso=%s, dataSaida=%s, duracao=%s]".formatted(id, pagina, referer, userAgent,
                                                                                                                               ip, dataAcesso, dataSaida,
                                                                                                                               duracao);
    }
}