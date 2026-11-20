package de.hsw.spieltag.daten;
import de.hsw.spieltag.modell.Spiel;
import de.hsw.spieltag.modell.SpielStatus;
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
/* SQL fuer die Tabelle SPIEL. */
public class SpielRepository {
private static final String SPALTEN = "spiel_id, gegner, spieldatum, status, tore_eigen, tore_gegner";
public int einfuegen(Connection c, String gegner, LocalDate datum) throws SQLException {
    String sql = "INSERT INTO spiel (gegner, spieldatum) VALUES (?, ?)";
    try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, gegner);
        ps.setDate(2, Date.valueOf(datum));
        ps.executeUpdate();
        try (ResultSet schluessel = ps.getGeneratedKeys()) {
            schluessel.next();
            return schluessel.getInt(1);
        }
    }
}
public Optional<Spiel> finde(Connection c, int id) throws SQLException {
    String sql = "SELECT " + SPALTEN + " FROM spiel WHERE spiel_id = ?";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, id);
        return lesen(ps).stream().findFirst();
    }
}
public List<Spiel> alle(Connection c) throws SQLException {
    String sql = "SELECT " + SPALTEN + " FROM spiel ORDER BY spieldatum";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
        return lesen(ps);
    }
}
/** Parametrisierte Suche: Spiele in einem Zeitraum. */
public List<Spiel> sucheNachDatum(Connection c, LocalDate von, LocalDate bis) throws SQLException {
    String sql = "SELECT " + SPALTEN + " FROM spiel WHERE spieldatum BETWEEN ? AND ? ORDER BY spieldatum";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setDate(1, Date.valueOf(von));
        ps.setDate(2, Date.valueOf(bis));
        return lesen(ps);
    }
}
        public void statusSetzen(Connection c, int id, SpielStatus status) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement("UPDATE spiel SET status = ? WHERE spiel_id = ?")) {
                ps.setString(1, status.text());
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        }
        public void ergebnisSetzen(Connection c, int id, int toreEigen, int toreGegner) throws SQLException {
            String sql = "UPDATE spiel SET tore_eigen = ?, tore_gegner = ? WHERE spiel_id = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, toreEigen);
                ps.setInt(2, toreGegner);
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        }
        private List<Spiel> lesen(PreparedStatement ps) throws SQLException {
            List<Spiel> liste = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Spiel(
                            rs.getInt("spiel_id"),
                            rs.getString("gegner"),
                            rs.getDate("spieldatum").toLocalDate(),
                            SpielStatus.vonText(rs.getString("status")),
                            ganzzahlOderNull(rs, "tore_eigen"),
                            ganzzahlOderNull(rs, "tore_gegner")));
                }
            }
            return liste;
        }
        private static Integer ganzzahlOderNull(ResultSet rs, String spalte) throws SQLException {
            int wert = rs.getInt(spalte);
            return rs.wasNull() ? null : wert;
        }
}