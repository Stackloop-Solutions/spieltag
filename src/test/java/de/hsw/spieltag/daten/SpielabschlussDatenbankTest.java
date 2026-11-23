package de.hsw.spieltag.daten;

import de.hsw.spieltag.logik.Ergebnis;
import de.hsw.spieltag.logik.SpielabschlussService;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Datenbanktests zu Anwendungsfall 3: Spiel abschliessen (Spiel 2 ist aufgestellt, Spieltag 29.11.2026). */
class SpielabschlussDatenbankTest extends DatenbankTest {

    private SpielabschlussService service(SpielerRepository spieler) {
        return new SpielabschlussService(db, new SpielRepository(), spieler, new AufstellungRepository());
    }

    @Test
    void abschlussMitRoterKarteSpeichertErgebnisUndSperrtDenSpieler() {
        Ergebnis ergebnis = service(new SpielerRepository()).abschliessen(2, 0, 3, Set.of(5));

        assertInstanceOf(Ergebnis.Erfolg.class, ergebnis);
        assertEquals(1, anzahl("SELECT COUNT(*) FROM spiel WHERE spiel_id = ? AND status = ? AND tore_eigen = ? AND tore_gegner = ?",
                2, "abgeschlossen", 0, 3));
        // 29.11.2026 plus 7 Tage
        assertEquals(1, anzahl("SELECT COUNT(*) FROM spieler WHERE spieler_id = ? AND gesperrt_bis = ?",
                5, java.sql.Date.valueOf(LocalDate.of(2026, 12, 6))));
        assertEquals(1, anzahl("SELECT COUNT(*) FROM aufstellung WHERE spiel_id = ? AND spieler_id = ? AND rote_karte = TRUE", 2, 5));
    }

    @Test
    void scheitertDasSetzenDerSperreWirdAllesZurueckgenommen() {
        // Test-Variante des Repositorys, deren letzter Schreibschritt absichtlich fehlschlaegt
        SpielerRepository defekt = new SpielerRepository() {
            @Override
            public void sperreSetzen(Connection c, int id, LocalDate bis) throws SQLException {
                throw new SQLException("absichtlicher Fehler im Test");
            }
        };

        assertThrows(DatenbankException.class, () -> service(defekt).abschliessen(2, 0, 3, Set.of(5)));

        // Ergebnis, Status und rote Karte duerfen nicht gespeichert sein, niemand ist gesperrt
        assertEquals(1, anzahl("SELECT COUNT(*) FROM spiel WHERE spiel_id = ? AND status = ? AND tore_eigen IS NULL", 2, "aufgestellt"));
        assertEquals(0, anzahl("SELECT COUNT(*) FROM aufstellung WHERE spiel_id = ? AND rote_karte = TRUE", 2));
        assertEquals(1, anzahl("SELECT COUNT(*) FROM spieler WHERE gesperrt_bis IS NOT NULL"));
    }
}