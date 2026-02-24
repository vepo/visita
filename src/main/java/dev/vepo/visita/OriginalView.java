package dev.vepo.visita;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_views_original_referer")
public class OriginalView {
    @Id
    @Column(name = "view_id")
    private Long id;

    @Column(name = "original_referer")
    private String referer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    @Override
    public String toString() {
        return "OriginalView[id=%d, referer=%s]".formatted(id, referer);
    }
}
