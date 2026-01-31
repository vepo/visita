package dev.vepo.visita.infra;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension
public class VisitaTemplateExtension {
    public static String formatSecondsToHHMMSS(Number value) {
        if (value == null) {
            return "00:00:00";
        }

        int seconds = value.intValue();

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public static String timestamp(Number value) {
        if (value == null) {
            return "0";
        }

        return Integer.toString(value.intValue());
    }
}
