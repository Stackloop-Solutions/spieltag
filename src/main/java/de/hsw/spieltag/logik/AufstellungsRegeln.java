package de.hsw.spieltag.logik;

import de.hsw.spieltag.modell.Position;
import de.hsw.spieltag.modell.Spiel;
import de.hsw.spieltag.modell.SpielStatus;
import de.hsw.spieltag.modell.Spieler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class AufstellungsRegeln {

    public static final int ANZAHL_STARTER = 11;

    private AufstellungsRegeln() {
    }

    public static Optional<String> pruefe(
            Spiel spiel,
            List<Spieler> starter,
            List<Spieler> bank) {

        if (spiel.status() != SpielStatus.GEPLANT) {
            return Optional.of(
                    "Das Spiel ist nicht mehr geplant (Status: "
                            + spiel.status().text() + ").");
        }

        List<Spieler> alle = new ArrayList<>(starter);
        alle.addAll(bank);

        Set<Integer> gesehen = new HashSet<>();
        for (Spieler spieler : alle) {
            if (!gesehen.add(spieler.id())) {
                return Optional.of(
                        spieler.name() + " steht mehrfach in der Aufstellung.");
            }
        }

        if (starter.size() != ANZAHL_STARTER) {
            return Optional.of(
                    "Es werden genau " + ANZAHL_STARTER
                            + " Startspieler benötigt, gewählt sind "
                            + starter.size() + ".");
        }

        if (starter.stream()
                .noneMatch(s -> s.position() == Position.TORWART)) {
            return Optional.of(
                    "Unter den Startspielern muss mindestens ein Torwart sein.");
        }

        for (Spieler spieler : alle) {
            if (spieler.istGesperrtAm(spiel.datum())) {
                return Optional.of(
                        spieler.name() + " ist am Spieltag gesperrt (bis "
                                + spieler.gesperrtBis() + ").");
            }
        }

        return Optional.empty();
    }
}
