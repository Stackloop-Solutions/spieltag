package de.hsw.spieltag.modell;
public enum Position {
    TORWART("Torwart"),
    ABWEHR("Abwehr"),
    MITTELFELD("Mittelfeld"),
    STURM("Sturm");
    private final String text;
    Position(String text) {
        this.text = text;
    }
    /** Text, so wie er in der Datenbank steht. */
    public String text() {
        return text;
    }
    public static Position vonText(String text) {
        for (Position position : values()) {
            if (position.text.equalsIgnoreCase(text.trim())) {
                return position;
            }
        }
        throw new IllegalArgumentException("Unbekannte Position: " + text);
    }
}