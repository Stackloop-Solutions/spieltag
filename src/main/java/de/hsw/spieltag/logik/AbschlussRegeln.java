package de.hsw.spieltag.logik;

import de.hsw.spieltag.modell.Spiel;
import de.hsw.spieltag.modell.SpielStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

/** Fachregeln fuer den Spielabschluss. Reine Logik, kein SQL. */
public final class AbschlussRegeln {

    public static final int SPERRE_IN_TAGEN = 7;

    private AbschlussRegeln() {
    }

    public static Optional<String> pruefe(Spiel spiel, int toreEigen, int toreGegner,
                                          Set<Integer> roteKartenSpielerIds, Set<Integer> aufgestellteSpielerIds) {
        if (spiel.status() != SpielStatus.AUFGESTELLT) {
            return Optional.of("Das Spiel ist nicht aufgestellt (Status: " + spiel.status().text() + ").");
        }
        if (toreEigen < 0 || toreGegner < 0) {
            return Optional.of("Die Tore dürfen nicht negativ sein.");
        }
        for (int spielerId : roteKartenSpielerIds) {
            if (!aufgestellteSpielerIds.contains(spielerId)) {
                return Optional.of("Spieler " + spielerId + " steht nicht in der Aufstellung dieses Spiels.");
            }
        }
        return Optional.empty();
    }

    /** Vereinfachte Regel: Eine rote Karte sperrt bis 7 Tage nach dem Spieldatum. */
    public static LocalDate sperreBis(LocalDate spieldatum) {
        return spieldatum.plusDays(SPERRE_IN_TAGEN);
    }
}