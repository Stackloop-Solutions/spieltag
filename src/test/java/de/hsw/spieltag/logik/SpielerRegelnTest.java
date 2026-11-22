package de.hsw.spieltag.logik;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit-Tests ohne Datenbank (Anwendungsfall 1). */
class SpielerRegelnTest {

    @Test
    void gueltigerSpielerWirdAkzeptiert() {
        Optional<String> fehler = SpielerRegeln.pruefe("Max Muster", 7);

        assertTrue(fehler.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100, -1})
    void trikotnummerAusserhalbVon1Bis99WirdAbgelehnt(int nummer) {
        Optional<String> fehler = SpielerRegeln.pruefe("Max Muster", nummer);

        assertTrue(fehler.isPresent());
        assertEquals("Die Trikotnummer muss zwischen 1 und 99 liegen.", fehler.get());
    }
}