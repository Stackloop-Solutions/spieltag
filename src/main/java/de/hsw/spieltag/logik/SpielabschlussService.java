package de.hsw.spieltag.logik;

import de.hsw.spieltag.daten.AufstellungRepository;
import de.hsw.spieltag.daten.Datenbank;
import de.hsw.spieltag.daten.SpielRepository;
import de.hsw.spieltag.daten.SpielerRepository;
import de.hsw.spieltag.modell.AufstellungsZeile;
import de.hsw.spieltag.modell.Spiel;
import de.hsw.spieltag.modell.SpielStatus;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Anwendungsfall 3: Spiel abschliessen. Eine Transaktion: Ergebnis, Status, rote Karten und Sperren. */
public class SpielabschlussService {

    private final Datenbank db;
    private final SpielRepository spiele;
    private final SpielerRepository spieler;
    private final AufstellungRepository aufstellungen;

    public SpielabschlussService(Datenbank db, SpielRepository spiele, SpielerRepository spieler,
                                 AufstellungRepository aufstellungen) {
        this.db = db;
        this.spiele = spiele;
        this.spieler = spieler;
        this.aufstellungen = aufstellungen;
    }

    public Ergebnis abschliessen(int spielId, int toreEigen, int toreGegner, Set<Integer> roteKartenSpielerIds) {
        return db.inTransaktion(c -> {
            Optional<Spiel> spiel = spiele.finde(c, spielId);
            if (spiel.isEmpty()) {
                return Ergebnis.abgelehnt("Spiel " + spielId + " nicht gefunden.");
            }

            Set<Integer> aufgestellte = new HashSet<>();
            List<AufstellungsZeile> zeilen = aufstellungen.zuSpiel(c, spielId);
            for (AufstellungsZeile zeile : zeilen) {
                aufgestellte.add(zeile.spielerId());
            }

            Optional<String> fehler = AbschlussRegeln.pruefe(
                    spiel.get(), toreEigen, toreGegner, roteKartenSpielerIds, aufgestellte);
            if (fehler.isPresent()) {
                return Ergebnis.abgelehnt(fehler.get());
            }

            // Ab hier wird geschrieben: bei jedem Fehler macht Datenbank.inTransaktion ein Rollback.
            LocalDate sperreBis = AbschlussRegeln.sperreBis(spiel.get().datum());
            spiele.ergebnisSetzen(c, spielId, toreEigen, toreGegner);
            spiele.statusSetzen(c, spielId, SpielStatus.ABGESCHLOSSEN);
            for (int spielerId : roteKartenSpielerIds) {
                aufstellungen.roteKarteSetzen(c, spielId, spielerId);
                spieler.sperreSetzen(c, spielerId, sperreBis);
            }
            return Ergebnis.ok("Spiel abgeschlossen (" + toreEigen + ":" + toreGegner + "), "
                    + roteKartenSpielerIds.size() + " Spieler gesperrt.");
        });
    }
}