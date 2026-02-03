package dev.vepo.visita.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_domains")
public class Domain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String hostname;

    @Column(nullable = false)
    private String token;

    public Domain() {}

    public Domain(String hostname, String token) {
        Objects.requireNonNull(hostname, "'hostname' cannot be null!");
        Objects.requireNonNull(token, "'token' cannot be null!");
        this.hostname = hostname;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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
            return Objects.equals(((Domain) obj).id, id);
        }
    }

    @Override
    public String toString() {
        return "Domain[id=%d, hostname=%s]".formatted(id, hostname);
    }
}
