package de.hsw.spieltag.modell;

import java.time.LocalDate;

public record Spieler(int id, String name, Position position, int trikotnummer, LocalDate gesperrtBis) {

    /** Gesperrt heisst: Das Datum liegt vor oder am Ende der Sperre. */
    public boolean istGesperrtAm(LocalDate datum) {
        return gesperrtBis != null && !datum.isAfter(gesperrtBis);
    }
}