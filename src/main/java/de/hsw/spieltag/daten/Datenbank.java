package de.hsw.spieltag.daten;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.stream.Collectors;

/** Verbindung, Transaktionen und SQL-Skripte. Hier steht sonst nichts Fachliches. */
public class Datenbank {

    @FunctionalInterface
    public interface SqlAktion<T> {
        T ausfuehren(Connection verbindung) throws SQLException;
    }

    private final String url;

    public Datenbank(String url) {
        this.url = url;
    }

    /** Frische Datenbank im Arbeitsspeicher, fuer Tests und Messung. */
    public static Datenbank imSpeicher() {
        return new Datenbank("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    }

    /** Lokale Datei, fuer die Anwendung. */
    public static Datenbank ausDatei(String pfad) {
        return new Datenbank("jdbc:h2:file:" + pfad);
    }

    public Connection verbindung() throws SQLException {
        return DriverManager.getConnection(url, "sa", "");
    }

    /** Nur lesen: eigene Verbindung, wird danach geschlossen. */
    public <T> T lesen(SqlAktion<T> aktion) {
        try (Connection verbindung = verbindung()) {
            return aktion.ausfuehren(verbindung);
        } catch (SQLException e) {
            throw new DatenbankException("Datenbankfehler: " + e.getMessage(), e);
        }
    }

    /** Alles oder nichts: Commit bei Erfolg, Rollback bei jedem Fehler. */
    public <T> T inTransaktion(SqlAktion<T> aktion) {
        try (Connection verbindung = verbindung()) {
            verbindung.setAutoCommit(false);
            try {
                T ergebnis = aktion.ausfuehren(verbindung);
                verbindung.commit();
                return ergebnis;
            } catch (SQLException | RuntimeException e) {
                verbindung.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatenbankException("Datenbankfehler: " + e.getMessage(), e);
        }
    }

    public void schemaAnlegen() {
        skriptAusfuehren("/db/schema.sql");
    }

    public void testdatenLaden() {
        skriptAusfuehren("/db/testdaten.sql");
    }

    private void skriptAusfuehren(String ressource) {
        String sql = ressourceLesen(ressource).lines()
                .filter(zeile -> !zeile.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));
        inTransaktion(verbindung -> {
            try (Statement anweisung = verbindung.createStatement()) {
                for (String teil : sql.split(";")) {
                    if (!teil.isBlank()) {
                        anweisung.execute(teil.trim());
                    }
                }
            }
            return null;
        });
    }

    private String ressourceLesen(String ressource) {
        try (InputStream eingabe = Datenbank.class.getResourceAsStream(ressource)) {
            if (eingabe == null) {
                throw new DatenbankException("SQL-Skript nicht gefunden: " + ressource, null);
            }
            return new String(eingabe.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DatenbankException("SQL-Skript nicht lesbar: " + ressource, e);
        }
    }
}