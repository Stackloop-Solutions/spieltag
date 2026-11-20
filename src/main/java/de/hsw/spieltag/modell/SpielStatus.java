package de.hsw.spieltag.modell;

public enum SpielStatus {
    GEPLANT("geplant"),
    AUFGESTELLT("aufgestellt"),
    ABGESCHLOSSEN("abgeschlossen");

    private final String text;

    SpielStatus(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public static SpielStatus vonText(String text) {
        for (SpielStatus status : values()) {
            if (status.text.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unbekannter Status: " + text);
    }
}