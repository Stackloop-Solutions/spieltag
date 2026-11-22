package de.hsw.spieltag.logik;

import de.hsw.spieltag.modell.Position;
import de.hsw.spieltag.modell.Spiel;
import de.hsw.spieltag.modell.SpielStatus;
import de.hsw.spieltag.modell.Spieler;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AufstellungsRegelnTest {

    private static final LocalDate SPIELTAG =
            LocalDate.of(2026, 12, 6);

    private static final Spiel SPIEL =
            new Spiel(1, "TSV West", SPIELTAG,
                    SpielStatus.GEPLANT, null, null);

    private static Spieler spieler(
            int id, Position position, LocalDate gesperrtBis) {

        return new Spieler(
                id, "Spieler " + id, position, id, gesperrtBis);
    }

    // Erstellt elf Spieler: einen Torwart und zehn Feldspieler.
    private static List<Spieler> elf() {
        List<Spieler> liste = new ArrayList<>();
        liste.add(spieler(1, Position.TORWART, null));

        for (int id = 2; id <= 11; id++) {
            liste.add(spieler(id, Position.MITTELFELD, null));
        }

        return liste;
    }

    @Test
    void gueltigeAufstellungWirdAkzeptiert() {
        Optional<String> fehler = AufstellungsRegeln.pruefe(
                SPIEL, elf(),
                List.of(spieler(12, Position.STURM, null)));

        assertTrue(fehler.isEmpty());
    }

    @Test
    void zehnStartspielerWerdenAbgelehnt() {
        List<Spieler> zehn = elf().subList(0, 10);

        Optional<String> fehler = AufstellungsRegeln.pruefe(
                SPIEL, zehn, List.of());

        assertTrue(fehler.isPresent());
        assertTrue(fehler.get().contains("11"));
    }

    @Test
    void aufstellungOhneTorwartWirdAbgelehnt() {
        List<Spieler> ohneTorwart = elf();
        ohneTorwart.set(0, spieler(1, Position.ABWEHR, null));

        Optional<String> fehler = AufstellungsRegeln.pruefe(
                SPIEL, ohneTorwart, List.of());

        assertTrue(fehler.isPresent());
        assertTrue(fehler.get().contains("Torwart"));
    }

    @Test
    void gesperrterSpielerAufDerBankWirdAbgelehnt() {
        Spieler gesperrt = spieler(
                12, Position.STURM, SPIELTAG.plusDays(3));

        Optional<String> fehler = AufstellungsRegeln.pruefe(
                SPIEL, elf(), List.of(gesperrt));

        assertTrue(fehler.isPresent());
        assertTrue(fehler.get().contains("gesperrt"));
    }
}