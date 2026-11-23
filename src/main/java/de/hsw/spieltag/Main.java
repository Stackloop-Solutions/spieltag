package de.hsw.spieltag;

import de.hsw.spieltag.daten.AufstellungRepository;
import de.hsw.spieltag.daten.Datenbank;
import de.hsw.spieltag.daten.SpielRepository;
import de.hsw.spieltag.daten.SpielerRepository;
import de.hsw.spieltag.logik.AufstellungService;
import de.hsw.spieltag.logik.KaderService;
import de.hsw.spieltag.logik.SpielService;
import de.hsw.spieltag.logik.SpielabschlussService;
import de.hsw.spieltag.ui.Konsole;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Datenbank db = Datenbank.ausDatei("./daten/spieltag");
        db.schemaAnlegen();

        boolean leer = db.lesen(c -> {
            try (Statement anweisung = c.createStatement();
                 ResultSet rs = anweisung.executeQuery("SELECT COUNT(*) FROM spieler")) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        });
        if (leer) {
            db.testdatenLaden();
            System.out.println("Die Datenbank war leer, Testdaten wurden geladen.");
        }

        SpielerRepository spieler = new SpielerRepository();
        SpielRepository spiele = new SpielRepository();
        AufstellungRepository aufstellungen = new AufstellungRepository();

        Konsole konsole = new Konsole(
                new KaderService(db, spieler),
                new SpielService(db, spiele),
                new AufstellungService(db, spiele, spieler, aufstellungen),
                new SpielabschlussService(db, spiele, spieler, aufstellungen),
                new Scanner(System.in),
                System.out);
        konsole.starten();
    }
}