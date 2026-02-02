package dev.vepo.visita;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import dev.vepo.visita.page.Page;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_views")
public class View {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private Page page;

    private String referrer;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "access_timestamp")
    private LocalDateTime accessTimestamp;

    @Column(name = "end_timestamp")
    private LocalDateTime endTimestamp;

    @Column(name = "length")
    private Long length; // em segundos

    private String timezone;

    public View() {}

    public View(Page page, String referrer, String userAgent, String timezone, long timestamp) {
        this.page = page;
        this.referrer = referrer;
        this.timezone = timezone;
        this.userAgent = userAgent;
        this.accessTimestamp = Instant.ofEpochMilli(timestamp)
                                      .atZone(ZoneId.systemDefault())
                                      .toLocalDateTime();
    }

    public View(Page page, long timestamp, View extended) {
        this.page = page;
        this.referrer = extended.referrer;
        this.userAgent = extended.userAgent;
        this.timezone = extended.timezone;
        this.accessTimestamp = Instant.ofEpochMilli(timestamp)
                                      .atZone(ZoneId.systemDefault())
                                      .toLocalDateTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public boolean isSamePage(String path) {
        return Objects.nonNull(page) && Objects.equals(path, page.getPath());
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

    public LocalDateTime getAccessTimestamp() {
        return accessTimestamp;
    }

    public void setAccessTimestamp(LocalDateTime accessTimestamp) {
        this.accessTimestamp = accessTimestamp;
    }

    public LocalDateTime getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(LocalDateTime dataSaida) {
        this.endTimestamp = dataSaida;
        if (this.accessTimestamp != null && this.endTimestamp != null) {
            this.length = ChronoUnit.SECONDS.between(this.accessTimestamp, this.endTimestamp);
        }
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public void extendDuration(long timestamp) {
        this.length = ChronoUnit.SECONDS.between(this.accessTimestamp, Instant.ofEpochMilli(timestamp)
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
            return Objects.equals(((View) obj).id, id);
        }
    }

    @Override
    public String toString() {
        return "Visita [id=%s, page=%s, referrer=%s, userAgent=%s, timezone=%s, accessTimestamp=%s, endTimestamp=%s, length=%s]".formatted(id, page, referrer,
                                                                                                                                           userAgent,
                                                                                                                                           timezone,
                                                                                                                                           accessTimestamp,
                                                                                                                                           endTimestamp,
                                                                                                                                           length);
    }
}