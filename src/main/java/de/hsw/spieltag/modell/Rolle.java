package de.hsw.spieltag.modell;

public enum Rolle {
    START("Start"),
    BANK("Bank");

    private final String text;

    Rolle(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public static Rolle vonText(String text) {
        for (Rolle rolle : values()) {
            if (rolle.text.equalsIgnoreCase(text.trim())) {
                return rolle;
            }
        }
        throw new IllegalArgumentException("Unbekannte Rolle: " + text);
    }
}