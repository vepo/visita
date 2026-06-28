package dev.vepo.visita.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

public final class IgnoredPathPatterns {

    public static final int MAX_PATTERNS = 20;
    public static final int MAX_PATTERN_LENGTH = 255;

    private IgnoredPathPatterns() {}

    public static List<String> parse(String stored) {
        if (Objects.isNull(stored) || stored.isBlank()) {
            return List.of();
        }
        return stored.lines()
                     .map(String::trim)
                     .filter(line -> !line.isEmpty())
                     .toList();
    }

    public static String serialize(List<String> patterns) {
        if (Objects.isNull(patterns) || patterns.isEmpty()) {
            return null;
        }
        validate(patterns);
        return patterns.stream()
                       .map(String::trim)
                       .filter(line -> !line.isEmpty())
                       .collect(Collectors.joining("\n"));
    }

    public static void validate(List<String> patterns) {
        if (Objects.isNull(patterns) || patterns.isEmpty()) {
            return;
        }
        if (patterns.size() > MAX_PATTERNS) {
            throw new WebApplicationException("Too many ignored path patterns (max %d)".formatted(MAX_PATTERNS),
                                              Status.BAD_REQUEST);
        }
        for (var pattern : patterns) {
            if (Objects.isNull(pattern) || pattern.isBlank()) {
                continue;
            }
            var trimmed = pattern.trim();
            if (trimmed.length() > MAX_PATTERN_LENGTH) {
                throw new WebApplicationException("Ignored path pattern exceeds max length of %d".formatted(MAX_PATTERN_LENGTH),
                                                  Status.BAD_REQUEST);
            }
            try {
                Pattern.compile(trimmed);
            } catch (PatternSyntaxException exception) {
                throw new WebApplicationException("Invalid ignored path pattern: %s".formatted(trimmed),
                                                  Status.BAD_REQUEST);
            }
        }
    }

    public static boolean matches(String path, String storedPatterns) {
        if (Objects.isNull(path) || path.isBlank()) {
            return false;
        }
        var patterns = compile(storedPatterns);
        for (var pattern : patterns) {
            if (pattern.matcher(path).find()) {
                return true;
            }
        }
        return false;
    }

    private static List<Pattern> compile(String storedPatterns) {
        var source = parse(storedPatterns);
        if (source.isEmpty()) {
            return List.of();
        }
        var compiled = new ArrayList<Pattern>();
        for (var line : source) {
            compiled.add(Pattern.compile(line));
        }
        return Collections.unmodifiableList(compiled);
    }
}
