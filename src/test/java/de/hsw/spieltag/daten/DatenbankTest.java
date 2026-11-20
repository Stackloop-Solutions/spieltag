package de.hsw.spieltag.daten;

import org.junit.jupiter.api.BeforeEach;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Basis fuer alle Datenbanktests: Vor jedem Test entsteht eine frische H2-Datenbank im Arbeitsspeicher,
 * gebaut aus den SQL-Skripten. Dadurch ist jeder Test unabhaengig und wiederholbar.
 */
abstract class DatenbankTest {

    protected Datenbank db;

    @BeforeEach
    void datenbankAufbauen() {
        db = Datenbank.imSpeicher();
        db.schemaAnlegen();
        db.testdatenLaden();
    }

    /** Zaehlt Zeilen, zum Beispiel: anzahl("SELECT COUNT(*) FROM spieler WHERE position = ?", "Torwart"). */
    protected int anzahl(String sql, Object... parameter) {
        return db.lesen(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int i = 0; i < parameter.length; i++) {
                    ps.setObject(i + 1, parameter[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        });
    }
}