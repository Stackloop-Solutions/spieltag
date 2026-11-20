package de.hsw.spieltag.logik;

/**
 * Ergebnis eines Anwendungsfalls. Eine fachliche Ablehnung ist ein normaler Rueckgabewert,
 * ein technischer Fehler ist dagegen eine DatenbankException.
 */
public sealed interface Ergebnis {

    record Erfolg(String meldung) implements Ergebnis {
    }

    record Abgelehnt(String grund) implements Ergebnis {
    }

    static Ergebnis ok(String meldung) {
        return new Erfolg(meldung);
    }

    static Ergebnis abgelehnt(String grund) {
        return new Abgelehnt(grund);
    }
}