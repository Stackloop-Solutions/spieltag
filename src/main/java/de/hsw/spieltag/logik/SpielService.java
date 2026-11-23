package de.hsw.spieltag.logik;

import de.hsw.spieltag.daten.Datenbank;
import de.hsw.spieltag.daten.SpielRepository;
import de.hsw.spieltag.modell.Spiel;

import java.time.LocalDate;
import java.util.List;

/** Spiele planen und anzeigen. */
public class SpielService {

    private final Datenbank db;
    private final SpielRepository spiele;

    public SpielService(Datenbank db, SpielRepository spiele) {
        this.db = db;
        this.spiele = spiele;
    }

    public Ergebnis planen(String gegner, LocalDate datum) {
        if (gegner == null || gegner.isBlank()) {
            return Ergebnis.abgelehnt(
                    "Der Gegner darf nicht leer sein.");
        }

        if (gegner.length() > 100) {
            return Ergebnis.abgelehnt(
                    "Der Gegner darf höchstens 100 Zeichen haben.");
        }

        return db.inTransaktion(c -> {
            int id = spiele.einfuegen(c, gegner.trim(), datum);

            return Ergebnis.ok(
                    "Spiel gegen " + gegner.trim()
                            + " mit der ID " + id + " geplant.");
        });
    }

    public List<Spiel> alle() {
        return db.lesen(spiele::alle);
    }

    public List<Spiel> sucheNachDatum(LocalDate von, LocalDate bis) {
        return db.lesen(c -> spiele.sucheNachDatum(c, von, bis));
    }
}