package dev.vepo.visita;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_views_original_referrer")
public class OriginalView {
    @Id
    @Column(name = "view_id")
    private Long id;

    @Column(name = "original_referrer")
    private String referrer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    @Override
    public String toString() {
        return "OriginalView[id=%d, referrer=%s]".formatted(id, referrer);
    }
}
