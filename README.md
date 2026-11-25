# Spieltag und Aufstellung

Konsolenanwendung für Amateurfußballvereine: Der Trainer verwaltet den Kader, erstellt Aufstellungen und schließt Spiele ab. Ungültige Aufstellungen werden verhindert (gesperrte Spieler, fehlender Torwart, falsche Anzahl), rote Karten sperren Spieler automatisch.

Projekt von Florian, Tim und Julian im Modul Fortgeschrittene Programmierung (HSW).

## Voraussetzungen

- JDK 21
- Git
- Internetzugang beim ersten Start (Gradle lädt die Abhängigkeiten, darunter H2 und JUnit)

Eine Datenbank muss nicht installiert werden. H2 läuft eingebettet und legt die Datei `daten/spieltag.mv.db` selbst an.

## Starten

```
./gradlew run          (Windows: gradlew.bat run)
```

Beim ersten Start ist die Datenbank leer. Die Anwendung baut dann das Schema aus `src/main/resources/db/schema.sql` auf und lädt die Testdaten aus `src/main/resources/db/testdaten.sql` (16 Spieler, 6 Spiele, 2 Aufstellungen). Zum Zurücksetzen den Ordner `daten/` löschen.

## Tests

```
./gradlew test         (Windows: gradlew.bat test)
```

16 Tests: 8 Unit-Tests der Fachregeln ohne Datenbank und 8 Datenbanktests. Jeder Datenbanktest baut vorher eine frische H2-Datenbank im Arbeitsspeicher aus den SQL-Skripten auf. Der Testbericht liegt danach unter `build/reports/tests/test/index.html`.

## Messung

```
./gradlew messung
```

Vergleicht das Speichern einer Aufstellung mit einem SQL-Aufruf pro Zeile gegen einen Batch. Ergebnisse stehen in `docs/Messung-und-Refactoring.md`.

## Demo in 3 Schritten

Nach `./gradlew run` im Menü:

1. **Kader verwalten:** `2` (Spieler anlegen), Name `Max Muster`, Position `Mittelfeld`, Nummer `7` ergibt „OK“. Dann noch einmal `2` mit Nummer `7` ergibt „Abgelehnt: Die Trikotnummer 7 ist schon vergeben.“
2. **Aufstellung erstellen:** `8`, Spiel `3`, Startspieler `1, 3, 4, 5, 6, 8, 9, 10, 11, 13, 14`, Bank `2, 7, 16` ergibt „Abgelehnt“, weil Hannes Roth (16) am Spieltag gesperrt ist. Noch einmal mit Bank `2, 7, 12` ergibt „OK“.
3. **Spiel abschließen:** `9`, Spiel `3`, Tore `2` und `1`, rote Karte für `5` ergibt „OK“. Mit `1` (Kader anzeigen) steht Nico Schulz jetzt als gesperrt bis 13.12.2026 in der Liste.

## Aufbau

| Paket | Aufgabe |
|---|---|
| `ui` | Konsole: Ein- und Ausgabe, kein SQL, keine Fachregeln |
| `logik` | Fachregeln und Anwendungsfälle, Transaktionen |
| `daten` | JDBC-Zugriff mit PreparedStatement, Transaktionshilfe, SQL-Skripte laden |
| `modell` | Records und Enums (Spieler, Spiel, Aufstellungszeile) |
| `messung` | Messprogramm zur Aufstellung |

Fachliche Ablehnungen (zum Beispiel gesperrter Spieler) sind ein normaler Rückgabewert (`Ergebnis.Abgelehnt`). Technische Fehler (zum Beispiel Datenbank nicht erreichbar) sind eine `DatenbankException`. Beim technischen Fehler wird die Transaktion zurückgenommen.

## Dokumente

- Projektsteckbrief: `docs/Projektsteckbrief.pdf`
- Anforderungszuordnung: `docs/Anforderungszuordnung.md`
- Messung und Refactoring: `docs/Messung-und-Refactoring.md`
- Quellen und KI-Hinweise: `docs/KI-und-Quellen.md`
