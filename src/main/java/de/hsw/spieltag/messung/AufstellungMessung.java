package de.hsw.spieltag.messung;

import de.hsw.spieltag.daten.AufstellungRepository;
import de.hsw.spieltag.daten.AufstellungRepository.Eintrag;
import de.hsw.spieltag.daten.Datenbank;
import de.hsw.spieltag.daten.SpielRepository;
import de.hsw.spieltag.modell.Rolle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reproduzierbare Messung zu Anwendungsfall 2:
 * Aufstellung speichern, eine Zeile pro SQL-Aufruf gegen alle Zeilen in einem Batch.
 * Start mit: gradlew messung
 */
public class AufstellungMessung {

    private static final int AUFSTELLUNGEN = 200;
    private static final int LAEUFE = 7;

    private static final List<Eintrag> EINTRAEGE = List.of(
            start(1), start(3), start(4), start(5), start(6), start(8), start(9), start(10),
            start(11), start(13), start(14),
            new Eintrag(2, Rolle.BANK), new Eintrag(7, Rolle.BANK), new Eintrag(12, Rolle.BANK));

    public static void main(String[] args) {
        SpielRepository spiele = new SpielRepository();
        AufstellungRepository aufstellungen = new AufstellungRepository();

        List<Long> einzeln = new ArrayList<>();
        List<Long> gesammelt = new ArrayList<>();

        // Der erste Lauf waermt die JVM auf und wird nicht gezaehlt.
        for (int lauf = 0; lauf <= LAEUFE; lauf++) {
            long zeitEinzeln = messen(spiele, aufstellungen, false);
            long zeitGesammelt = messen(spiele, aufstellungen, true);
            if (lauf > 0) {
                einzeln.add(zeitEinzeln);
                gesammelt.add(zeitGesammelt);
            }
        }

        int zeilen = EINTRAEGE.size();
        System.out.println("Messung: " + AUFSTELLUNGEN + " Aufstellungen mit je " + zeilen + " Zeilen speichern");
        System.out.println("Umgebung: Java " + System.getProperty("java.version") + ", "
                + System.getProperty("os.name") + ", H2 im Arbeitsspeicher, " + LAEUFE + " Läufe (Median)");
        System.out.println();
        System.out.println("| Variante | SQL-Aufrufe je Aufstellung | Median Gesamtzeit in ms |");
        System.out.println("|---|---|---|");
        System.out.printf("| einzeln (Vorher) | %d | %.1f |%n", zeilen, median(einzeln) / 1_000_000.0);
        System.out.printf("| gesammelt (Batch) | 1 | %.1f |%n", median(gesammelt) / 1_000_000.0);
    }

    /** Legt die Spiele vorher an (nicht gemessen) und misst nur das Speichern der Aufstellungen. */
    private static long messen(SpielRepository spiele, AufstellungRepository aufstellungen, boolean gesammelt) {
        Datenbank db = Datenbank.imSpeicher();
        db.schemaAnlegen();
        db.testdatenLaden();

        List<Integer> spielIds = db.inTransaktion(c -> {
            List<Integer> ids = new ArrayList<>();
            for (int i = 0; i < AUFSTELLUNGEN; i++) {
                ids.add(spiele.einfuegen(c, "Messgegner " + i, LocalDate.of(2027, 3, 1).plusDays(i)));
            }
            return ids;
        });

        long beginn = System.nanoTime();
        for (int spielId : spielIds) {
            db.inTransaktion(c -> {
                if (gesammelt) {
                    aufstellungen.einfuegenGesammelt(c, spielId, EINTRAEGE);
                } else {
                    for (Eintrag eintrag : EINTRAEGE) {
                        aufstellungen.einfuegen(c, spielId, eintrag.spielerId(), eintrag.rolle());
                    }
                }
                return null;
            });
        }
        return System.nanoTime() - beginn;
    }

    private static long median(List<Long> werte) {
        List<Long> sortiert = new ArrayList<>(werte);
        Collections.sort(sortiert);
        return sortiert.get(sortiert.size() / 2);
    }

    private static Eintrag start(int spielerId) {
        return new Eintrag(spielerId, Rolle.START);
    }
}