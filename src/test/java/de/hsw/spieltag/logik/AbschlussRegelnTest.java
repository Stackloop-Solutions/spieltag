package de.hsw.spieltag.logik;

import de.hsw.spieltag.modell.Spiel;
import de.hsw.spieltag.modell.SpielStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit-Tests ohne Datenbank (Anwendungsfall 3). */
class AbschlussRegelnTest {

    private static final Spiel AUFGESTELLT =
            new Spiel(2, "SV Ost", LocalDate.of(2026, 11, 29), SpielStatus.AUFGESTELLT, null, null);

    @Test
    void negativeToreWerdenAbgelehnt() {
        Optional<String> fehler = AbschlussRegeln.pruefe(AUFGESTELLT, -1, 0, Set.of(), Set.of(1, 2, 3));

        assertTrue(fehler.isPresent());
        assertEquals("Die Tore dürfen nicht negativ sein.", fehler.get());
    }

    @Test
    void roteKarteNurFuerSpielerDerAufstellung() {
        Optional<String> fehler = AbschlussRegeln.pruefe(AUFGESTELLT, 1, 0, Set.of(99), Set.of(1, 2, 3));

        assertTrue(fehler.isPresent());
        assertTrue(fehler.get().contains("99"));
    }
}