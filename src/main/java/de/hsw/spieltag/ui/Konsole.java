package de.hsw.spieltag.ui;

import de.hsw.spieltag.daten.DatenbankException;
import de.hsw.spieltag.logik.AufstellungService;
import de.hsw.spieltag.logik.Ergebnis;
import de.hsw.spieltag.logik.KaderService;
import de.hsw.spieltag.logik.SpielService;
import de.hsw.spieltag.logik.SpielabschlussService;
import de.hsw.spieltag.modell.AufstellungsZeile;
import de.hsw.spieltag.modell.Position;
import de.hsw.spieltag.modell.Spiel;
import de.hsw.spieltag.modell.Spieler;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/** Bedienung ueber die Konsole. Hier steht kein SQL und keine Fachregel, nur Ein- und Ausgabe. */
public class Konsole {

    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final KaderService kader;
    private final SpielService spiele;
    private final AufstellungService aufstellung;
    private final SpielabschlussService abschluss;
    private final Scanner eingabe;
    private final PrintStream ausgabe;

    public Konsole(KaderService kader, SpielService spiele, AufstellungService aufstellung,
                   SpielabschlussService abschluss, Scanner eingabe, PrintStream ausgabe) {
        this.kader = kader;
        this.spiele = spiele;
        this.aufstellung = aufstellung;
        this.abschluss = abschluss;
        this.eingabe = eingabe;
        this.ausgabe = ausgabe;
    }

    public void starten() {
        boolean weiter = true;
        while (weiter) {
            menueAnzeigen();
            String wahl = text("Auswahl");
            try {
                switch (wahl) {
                    case "1" -> kaderAnzeigen();
                    case "2" -> spielerAnlegen();
                    case "3" -> spielerSuchen();
                    case "4" -> spielerAendern();
                    case "5" -> spielerLoeschen();
                    case "6" -> spieleAnzeigen();
                    case "7" -> spielPlanen();
                    case "8" -> aufstellungErstellen();
                    case "9" -> spielAbschliessen();
                    case "0" -> weiter = false;
                    default -> ausgabe.println("Unbekannte Auswahl: " + wahl);
                }
            } catch (DatenbankException e) {
                ausgabe.println("Technischer Fehler: " + e.getMessage());
                ausgabe.println("Es wurde nichts geändert.");
            }
        }
        ausgabe.println("Tschüss!");
    }

    private void menueAnzeigen() {
        ausgabe.println();
        ausgabe.println("=== Spieltag und Aufstellung ===");
        ausgabe.println("1 Kader anzeigen");
        ausgabe.println("2 Spieler anlegen");
        ausgabe.println("3 Spieler suchen");
        ausgabe.println("4 Spieler ändern");
        ausgabe.println("5 Spieler löschen");
        ausgabe.println("6 Spiele anzeigen");
        ausgabe.println("7 Spiel planen");
        ausgabe.println("8 Aufstellung erstellen");
        ausgabe.println("9 Spiel abschließen");
        ausgabe.println("0 Beenden");
    }

    // ---- Anwendungsfall 1: Kader verwalten

    private void kaderAnzeigen() {
        spielerAusgeben(kader.alle());
    }

    private void spielerAnlegen() {
        String name = text("Name");
        Position position = position();
        int nummer = zahl("Trikotnummer");
        zeige(kader.anlegen(name, position, nummer));
    }

    private void spielerSuchen() {
        String art = text("Suchen nach (1 = Name, 2 = Position)");
        if (art.equals("1")) {
            spielerAusgeben(kader.sucheNachName(text("Teil des Namens")));
        } else if (art.equals("2")) {
            spielerAusgeben(kader.sucheNachPosition(position()));
        } else {
            ausgabe.println("Bitte 1 oder 2 eingeben.");
        }
    }

    private void spielerAendern() {
        int id = zahl("ID des Spielers");
        String name = text("Neuer Name");
        Position position = position();
        int nummer = zahl("Neue Trikotnummer");
        zeige(kader.aendern(id, name, position, nummer));
    }

    private void spielerLoeschen() {
        zeige(kader.loeschen(zahl("ID des Spielers")));
    }

    // ---- Spiele

    private void spieleAnzeigen() {
        List<Spiel> alle = spiele.alle();
        if (alle.isEmpty()) {
            ausgabe.println("Keine Spiele vorhanden.");
        }
        for (Spiel spiel : alle) {
            String ergebnis = spiel.toreEigen() == null ? "-" : spiel.toreEigen() + ":" + spiel.toreGegner();
            ausgabe.printf("%3d  %s  %-18s %-14s %s%n", spiel.id(), spiel.datum().format(DATUM),
                    spiel.gegner(), spiel.status().text(), ergebnis);
        }
    }

    private void spielPlanen() {
        String gegner = text("Gegner");
        LocalDate datum = datum("Spieldatum (TT.MM.JJJJ)");
        zeige(spiele.planen(gegner, datum));
    }

    // ---- Anwendungsfall 2: Aufstellung erstellen

    private void aufstellungErstellen() {
        spieleAnzeigen();
        int spielId = zahl("ID des Spiels");
        kaderAnzeigen();
        List<Integer> starter = zahlenListe("IDs der 11 Startspieler (mit Komma getrennt)");
        List<Integer> bank = zahlenListe("IDs der Bankspieler (mit Komma getrennt, leer = keine)");
        zeige(aufstellung.erstellen(spielId, starter, bank));
    }

    // ---- Anwendungsfall 3: Spiel abschliessen

    private void spielAbschliessen() {
        spieleAnzeigen();
        int spielId = zahl("ID des Spiels");
        int toreEigen = zahl("Eigene Tore");
        int toreGegner = zahl("Gegentore");
        Set<Integer> rote = new LinkedHashSet<>(zahlenListe("Spieler-IDs mit roter Karte (mit Komma getrennt, leer = keine)"));
        zeige(abschluss.abschliessen(spielId, toreEigen, toreGegner, rote));
        List<AufstellungsZeile> zeilen = aufstellung.zeilen(spielId);
        ausgabe.println("(Aufstellung dieses Spiels: " + zeilen.size() + " Zeilen)");
    }

    // ---- Ausgabe

    private void zeige(Ergebnis ergebnis) {
        switch (ergebnis) {
            case Ergebnis.Erfolg erfolg -> ausgabe.println("OK: " + erfolg.meldung());
            case Ergebnis.Abgelehnt abgelehnt -> ausgabe.println("Abgelehnt: " + abgelehnt.grund());
        }
    }

    private void spielerAusgeben(List<Spieler> liste) {
        if (liste.isEmpty()) {
            ausgabe.println("Keine Spieler gefunden.");
        }
        for (Spieler spieler : liste) {
            String sperre = spieler.gesperrtBis() == null ? "" : "gesperrt bis " + spieler.gesperrtBis().format(DATUM);
            ausgabe.printf("%3d  %-18s %-11s Nr. %2d  %s%n", spieler.id(), spieler.name(),
                    spieler.position().text(), spieler.trikotnummer(), sperre);
        }
    }

    // ---- Eingabe: ungueltige Eingaben werden hier abgefangen und erneut abgefragt

    private String text(String frage) {
        ausgabe.print(frage + ": ");
        return eingabe.nextLine().trim();
    }

    private int zahl(String frage) {
        while (true) {
            String antwort = text(frage);
            try {
                return Integer.parseInt(antwort);
            } catch (NumberFormatException e) {
                ausgabe.println("Bitte eine ganze Zahl eingeben.");
            }
        }
    }

    private List<Integer> zahlenListe(String frage) {
        while (true) {
            String antwort = text(frage);
            List<Integer> zahlen = new ArrayList<>();
            try {
                if (!antwort.isBlank()) {
                    for (String teil : antwort.split(",")) {
                        zahlen.add(Integer.parseInt(teil.trim()));
                    }
                }
                return zahlen;
            } catch (NumberFormatException e) {
                ausgabe.println("Bitte ganze Zahlen mit Komma getrennt eingeben, zum Beispiel 1, 3, 4.");
            }
        }
    }

    private LocalDate datum(String frage) {
        while (true) {
            try {
                return LocalDate.parse(text(frage), DATUM);
            } catch (DateTimeParseException e) {
                ausgabe.println("Bitte ein Datum im Format TT.MM.JJJJ eingeben, zum Beispiel 06.12.2026.");
            }
        }
    }

    private Position position() {
        while (true) {
            try {
                return Position.vonText(text("Position (Torwart, Abwehr, Mittelfeld, Sturm)"));
            } catch (IllegalArgumentException e) {
                ausgabe.println("Bitte eine der vier Positionen eingeben.");
            }
        }
    }
}
