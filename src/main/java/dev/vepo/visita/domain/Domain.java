package dev.vepo.visita.domain;

import java.util.List;
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

    private boolean disabled;

    @Column(name = "ignored_path_patterns")
    private String ignoredPathPatterns;

    public Domain() {
        this.disabled = false;
    }

    public Domain(String hostname, String token) {
        this.hostname = Objects.requireNonNull(hostname, "'hostname' cannot be null!");
        this.token = Objects.requireNonNull(token, "'token' cannot be null!");
        this.disabled = false;
    }

    public Domain(String hostname, String token, List<String> ignoredPathPatterns) {
        this(hostname, token);
        applyIgnoredPathPatterns(ignoredPathPatterns);
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getIgnoredPathPatterns() {
        return ignoredPathPatterns;
    }

    public void setIgnoredPathPatterns(String ignoredPathPatterns) {
        this.ignoredPathPatterns = ignoredPathPatterns;
    }

    public List<String> parsedIgnoredPathPatterns() {
        return IgnoredPathPatterns.parse(ignoredPathPatterns);
    }

    public void applyIgnoredPathPatterns(List<String> patterns) {
        IgnoredPathPatterns.validate(patterns);
        this.ignoredPathPatterns = IgnoredPathPatterns.serialize(patterns);
    }

    public boolean ignoresPath(String path) {
        return IgnoredPathPatterns.matches(path, ignoredPathPatterns);
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
        return "Domain[id=%d, hostname=%s, token=%s, disabled=%b]".formatted(id, hostname, token, disabled);
    }
}
