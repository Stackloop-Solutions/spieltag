package de.hsw.spieltag.logik;

import de.hsw.spieltag.daten.Datenbank;
import de.hsw.spieltag.daten.SpielerRepository;
import de.hsw.spieltag.modell.Position;
import de.hsw.spieltag.modell.Spieler;

import java.util.List;
import java.util.Optional;

/** Anwendungsfall 1: Kader verwalten. */
public class KaderService {

    private final Datenbank db;
    private final SpielerRepository spieler;

    public KaderService(Datenbank db, SpielerRepository spieler) {
        this.db = db;
        this.spieler = spieler;
    }

    public Ergebnis anlegen(String name, Position position, int trikotnummer) {
        Optional<String> fehler = SpielerRegeln.pruefe(name, trikotnummer);
        if (fehler.isPresent()) {
            return Ergebnis.abgelehnt(fehler.get());
        }
        return db.inTransaktion(c -> {
            if (spieler.trikotnummerVergeben(c, trikotnummer, 0)) {
                return Ergebnis.abgelehnt("Die Trikotnummer " + trikotnummer + " ist schon vergeben.");
            }
            int id = spieler.einfuegen(c, name.trim(), position, trikotnummer);
            return Ergebnis.ok("Spieler " + name.trim() + " mit der ID " + id + " angelegt.");
        });
    }

    public Ergebnis aendern(int id, String name, Position position, int trikotnummer) {
        Optional<String> fehler = SpielerRegeln.pruefe(name, trikotnummer);
        if (fehler.isPresent()) {
            return Ergebnis.abgelehnt(fehler.get());
        }
        return db.inTransaktion(c -> {
            if (spieler.finde(c, id).isEmpty()) {
                return Ergebnis.abgelehnt("Spieler " + id + " nicht gefunden.");
            }
            if (spieler.trikotnummerVergeben(c, trikotnummer, id)) {
                return Ergebnis.abgelehnt("Die Trikotnummer " + trikotnummer + " ist schon vergeben.");
            }
            spieler.aendern(c, id, name.trim(), position, trikotnummer);
            return Ergebnis.ok("Spieler " + id + " geändert.");
        });
    }

    public Ergebnis loeschen(int id) {
        return db.inTransaktion(c -> {
            if (spieler.finde(c, id).isEmpty()) {
                return Ergebnis.abgelehnt("Spieler " + id + " nicht gefunden.");
            }
            if (spieler.istInAufstellung(c, id)) {
                return Ergebnis.abgelehnt("Der Spieler steht in einer Aufstellung und kann nicht gelöscht werden.");
            }
            spieler.loeschen(c, id);
            return Ergebnis.ok("Spieler " + id + " gelöscht.");
        });
    }

    public List<Spieler> alle() {
        return db.lesen(spieler::alle);
    }

    public List<Spieler> sucheNachName(String teil) {
        return db.lesen(c -> spieler.sucheNachName(c, teil));
    }

    public List<Spieler> sucheNachPosition(Position position) {
        return db.lesen(c -> spieler.sucheNachPosition(c, position));
    }
}