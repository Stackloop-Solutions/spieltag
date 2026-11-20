package de.hsw.spieltag.daten;
import de.hsw.spieltag.modell.Position;
import de.hsw.spieltag.modell.Spieler;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/* SQL fuer die Tabelle SPIELER. Immer mit PreparedStatement, nie mit zusammengesetztem Text. */
public class SpielerRepository {
private static final String SPALTEN = "spieler_id, name, position, trikotnummer, gesperrt_bis";
public int einfuegen(Connection c, String name, Position position, int trikotnummer) throws SQLException {
    String sql = "INSERT INTO spieler (name, position, trikotnummer) VALUES (?, ?, ?)";
    try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, name);
        ps.setString(2, position.text());
        ps.setInt(3, trikotnummer);
        ps.executeUpdate();
        try (ResultSet schluessel = ps.getGeneratedKeys()) {
            schluessel.next();
            return schluessel.getInt(1);
        }
    }
}
public Optional<Spieler> finde(Connection c, int id) throws SQLException {
    String sql = "SELECT " + SPALTEN + " FROM spieler WHERE spieler_id = ?";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Date sperre = rs.getDate("gesperrt_bis");
                return Optional.of(new Spieler(
                        rs.getInt("spieler_id"),
                        rs.getString("name"),
                        Position.vonText(rs.getString("position")),
                        rs.getInt("trikotnummer"),
                        sperre == null ? null : sperre.toLocalDate()));
            }
            return Optional.empty();
        }
    }
}
public List<Spieler> alle(Connection c) throws SQLException {
    String sql = "SELECT " + SPALTEN + " FROM spieler ORDER BY trikotnummer";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
        List<Spieler> liste = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date sperre = rs.getDate("gesperrt_bis");
                liste.add(new Spieler(
                        rs.getInt("spieler_id"),
                        rs.getString("name"),
                        Position.vonText(rs.getString("position")),
                        rs.getInt("trikotnummer"),
                        sperre == null ? null : sperre.toLocalDate()));
            }
        }
        return liste;
    }
}
/** Parametrisierte Suche 1: Teil des Namens, ohne Gross- und Kleinschreibung. */
public List<Spieler> sucheNachName(Connection c, String teil) throws SQLException {
    String sql = "SELECT " + SPALTEN + " FROM spieler WHERE LOWER(name) LIKE ? ORDER BY name";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, "%" + teil.toLowerCase() + "%");
        List<Spieler> liste = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date sperre = rs.getDate("gesperrt_bis");
                liste.add(new Spieler(
                        rs.getInt("spieler_id"),
                        rs.getString("name"),
                        Position.vonText(rs.getString("position")),
                        rs.getInt("trikotnummer"),
                        sperre == null ? null : sperre.toLocalDate()));
            }
        }
        return liste;
    }
}
        /** Parametrisierte Suche 2: alle Spieler einer Position. */
        public List<Spieler> sucheNachPosition(Connection c, Position position) throws SQLException {
            String sql = "SELECT " + SPALTEN + " FROM spieler WHERE position = ? ORDER BY trikotnummer";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, position.text());
                List<Spieler> liste = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Date sperre = rs.getDate("gesperrt_bis");
                        liste.add(new Spieler(
                                rs.getInt("spieler_id"),
                                rs.getString("name"),
                                Position.vonText(rs.getString("position")),
                                rs.getInt("trikotnummer"),
                                sperre == null ? null : sperre.toLocalDate()));
                    }
                }
                return liste;
            }
        }
        /** Ist die Nummer bei einem anderen Spieler schon vergeben? (ausserSpielerId = 0: bei niemandem ausnehmen) */
        public boolean trikotnummerVergeben(Connection c, int trikotnummer, int ausserSpielerId) throws SQLException {
            String sql = "SELECT COUNT(*) FROM spieler WHERE trikotnummer = ? AND spieler_id <> ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, trikotnummer);
                ps.setInt(2, ausserSpielerId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }
        }
        public void aendern(Connection c, int id, String name, Position position, int trikotnummer) throws SQLException {
            String sql = "UPDATE spieler SET name = ?, position = ?, trikotnummer = ? WHERE spieler_id = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, position.text());
                ps.setInt(3, trikotnummer);
                ps.setInt(4, id);
                ps.executeUpdate();
            }
        }
        public void loeschen(Connection c, int id) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM spieler WHERE spieler_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
        public boolean istInAufstellung(Connection c, int id) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM aufstellung WHERE spieler_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }
        }
        public void sperreSetzen(Connection c, int id, LocalDate bis) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement("UPDATE spieler SET gesperrt_bis = ? WHERE spieler_id = ?")) {
                ps.setDate(1, Date.valueOf(bis));
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        }
}