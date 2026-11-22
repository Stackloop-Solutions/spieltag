package de.hsw.spieltag.daten;

import de.hsw.spieltag.logik.Ergebnis;
import de.hsw.spieltag.logik.KaderService;
import de.hsw.spieltag.modell.Position;
import de.hsw.spieltag.modell.Spieler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Datenbanktests zu Anwendungsfall 1: Kader verwalten. */
class KaderDatenbankTest extends DatenbankTest {

    private KaderService kader() {
        return new KaderService(db, new SpielerRepository());
    }

    @Test
    void neuerSpielerWirdGespeichertUndIstNichtGesperrt() {
        Ergebnis ergebnis = kader().anlegen("Max Muster", Position.MITTELFELD, 7);

        assertInstanceOf(Ergebnis.Erfolg.class, ergebnis);
        assertEquals(17, anzahl("SELECT COUNT(*) FROM spieler"));
        List<Spieler> gefunden = kader().sucheNachName("muster");
        assertEquals(1, gefunden.size());
        assertEquals(7, gefunden.get(0).trikotnummer());
        assertNull(gefunden.get(0).gesperrtBis());
    }

    @Test
    void vergebeneTrikotnummerWirdAbgelehntUndBestandBleibtGleich() {
        Ergebnis ergebnis = kader().anlegen("Doppelt", Position.ABWEHR, 1);

        assertInstanceOf(Ergebnis.Abgelehnt.class, ergebnis);
        assertEquals(16, anzahl("SELECT COUNT(*) FROM spieler"));
    }

    @Test
    void spielerInEinerAufstellungWirdNichtGeloescht() {
        Ergebnis ergebnis = kader().loeschen(1);

        assertInstanceOf(Ergebnis.Abgelehnt.class, ergebnis);
        assertEquals(1, anzahl("SELECT COUNT(*) FROM spieler WHERE spieler_id = ?", 1));
    }
}