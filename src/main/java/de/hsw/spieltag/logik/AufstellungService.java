package de.hsw.spieltag.logik;

import de.hsw.spieltag.daten.AufstellungRepository;
import de.hsw.spieltag.daten.Datenbank;
import de.hsw.spieltag.daten.SpielRepository;
import de.hsw.spieltag.daten.SpielerRepository;
import de.hsw.spieltag.modell.AufstellungsZeile;
import de.hsw.spieltag.modell.Rolle;
import de.hsw.spieltag.modell.Spiel;
import de.hsw.spieltag.modell.SpielStatus;
import de.hsw.spieltag.modell.Spieler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Aufstellung und Spielstatus gemeinsam speichern. */
public class AufstellungService {

    private final Datenbank db;
    private final SpielRepository spiele;
    private final SpielerRepository spieler;
    private final AufstellungRepository aufstellungen;

    public AufstellungService(
            Datenbank db,
            SpielRepository spiele,
            SpielerRepository spieler,
            AufstellungRepository aufstellungen) {

        this.db = db;
        this.spiele = spiele;
        this.spieler = spieler;
        this.aufstellungen = aufstellungen;
    }

    public Ergebnis erstellen(
            int spielId,
            List<Integer> starterIds,
            List<Integer> bankIds) {

        return db.inTransaktion(c -> {
            Optional<Spiel> spiel = spiele.finde(c, spielId);

            if (spiel.isEmpty()) {
                return Ergebnis.abgelehnt(
                        "Spiel " + spielId + " nicht gefunden.");
            }

            List<Spieler> starter = laden(c, starterIds);
            List<Spieler> bank = laden(c, bankIds);

            if (starter.size() != starterIds.size()
                    || bank.size() != bankIds.size()) {

                return Ergebnis.abgelehnt(
                        "Mindestens ein gewählter Spieler existiert nicht.");
            }

            Optional<String> fehler =
                    AufstellungsRegeln.pruefe(spiel.get(), starter, bank);

            if (fehler.isPresent()) {
                return Ergebnis.abgelehnt(fehler.get());
            }

            // Erst nach allen Prüfungen wird gespeichert.
            for (Spieler s : starter) {
                aufstellungen.einfuegen(
                        c, spielId, s.id(), Rolle.START);
            }

            for (Spieler s : bank) {
                aufstellungen.einfuegen(
                        c, spielId, s.id(), Rolle.BANK);
            }

            spiele.statusSetzen(
                    c, spielId, SpielStatus.AUFGESTELLT);

            return Ergebnis.ok(
                    "Aufstellung für das Spiel gegen "
                            + spiel.get().gegner() + " gespeichert.");
        });
    }

    public List<AufstellungsZeile> zeilen(int spielId) {
        return db.lesen(c -> aufstellungen.zuSpiel(c, spielId));
    }

    private List<Spieler> laden(
            Connection c, List<Integer> ids) throws SQLException {

        List<Spieler> liste = new ArrayList<>();

        for (int id : ids) {
            spieler.finde(c, id).ifPresent(liste::add);
        }

        return liste;
    }
}