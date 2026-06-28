package dev.vepo.visita.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.WebApplicationException;

class IgnoredPathPatternsTest {

    @Test
    void shouldMatchPathAgainstStoredPatterns() {
        assertThat(IgnoredPathPatterns.matches("/admin/users", "/admin/.*")).isTrue();
        assertThat(IgnoredPathPatterns.matches("/about", "/admin/.*")).isFalse();
    }

    @Test
    void shouldRejectInvalidPattern() {
        assertThatThrownBy(() -> IgnoredPathPatterns.validate(java.util.List.of("[invalid")))
                                                                                             .isInstanceOf(WebApplicationException.class);
    }
}
