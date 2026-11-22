package de.hsw.spieltag.daten;

import de.hsw.spieltag.logik.AufstellungService;
import de.hsw.spieltag.logik.Ergebnis;
import de.hsw.spieltag.modell.SpielStatus;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AufstellungDatenbankTest extends DatenbankTest {

    private static final List<Integer> STARTER =
            List.of(1, 3, 4, 5, 6, 8, 9, 10, 11, 13, 14);

    private static final List<Integer> BANK =
            List.of(2, 7, 12);

    private AufstellungService service(SpielRepository spiele) {
        return new AufstellungService(
                db,
                spiele,
                new SpielerRepository(),
                new AufstellungRepository());
    }

    @Test
    void gueltigeAufstellungWirdGespeichertUndSpielWirdAufgestellt() {
        Ergebnis ergebnis =
                service(new SpielRepository()).erstellen(3, STARTER, BANK);

        assertInstanceOf(Ergebnis.Erfolg.class, ergebnis);

        assertEquals(14, anzahl(
                "SELECT COUNT(*) FROM aufstellung WHERE spiel_id = ?",
                3));

        assertEquals(1, anzahl(
                "SELECT COUNT(*) FROM spiel WHERE spiel_id = ? AND status = ?",
                3, "aufgestellt"));
    }

    @Test
    void gesperrterSpielerFuehrtZurAblehnungOhneGespeichertesErgebnis() {
        Ergebnis ergebnis =
                service(new SpielRepository()).erstellen(
                        3, STARTER, List.of(2, 7, 16));

        assertInstanceOf(Ergebnis.Abgelehnt.class, ergebnis);

        assertEquals(0, anzahl(
                "SELECT COUNT(*) FROM aufstellung WHERE spiel_id = ?",
                3));

        assertEquals(1, anzahl(
                "SELECT COUNT(*) FROM spiel WHERE spiel_id = ? AND status = ?",
                3, "geplant"));
    }

    @Test
    void scheitertDerStatusSchreibenWirdAllesZurueckgenommen() {
        // Der Statuswechsel schlägt für diesen Test absichtlich fehl.
        SpielRepository defekt = new SpielRepository() {
            @Override
            public void statusSetzen(
                    Connection c, int id, SpielStatus status)
                    throws SQLException {

                throw new SQLException("absichtlicher Fehler im Test");
            }
        };

        assertThrows(
                DatenbankException.class,
                () -> service(defekt).erstellen(3, STARTER, BANK));

        // Die zuvor eingefügten Aufstellungszeilen müssen entfernt sein.
        assertEquals(0, anzahl(
                "SELECT COUNT(*) FROM aufstellung WHERE spiel_id = ?",
                3));

        // Das Spiel muss weiterhin geplant sein.
        assertEquals(1, anzahl(
                "SELECT COUNT(*) FROM spiel WHERE spiel_id = ? AND status = ?",
                3, "geplant"));
    }
}