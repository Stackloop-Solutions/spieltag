package de.hsw.spieltag.logik;

import java.util.Optional;

/** Fachregeln fuer Spieler. Reine Logik, kein SQL, deshalb ohne Datenbank testbar. */
public final class SpielerRegeln {

    private SpielerRegeln() {
    }

    /** Liefert den Ablehnungsgrund oder leer, wenn alles in Ordnung ist. */
    public static Optional<String> pruefe(String name, int trikotnummer) {
        if (name == null || name.isBlank()) {
            return Optional.of("Der Name darf nicht leer sein.");
        }
        if (name.length() > 100) {
            return Optional.of("Der Name darf höchstens 100 Zeichen haben.");
        }
        if (trikotnummer < 1 || trikotnummer > 99) {
            return Optional.of("Die Trikotnummer muss zwischen 1 und 99 liegen.");
        }
        return Optional.empty();
    }
}