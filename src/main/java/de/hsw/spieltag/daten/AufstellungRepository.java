package de.hsw.spieltag.daten;
import de.hsw.spieltag.modell.AufstellungsZeile;
import de.hsw.spieltag.modell.Rolle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/* SQL fuer die Tabelle AUFSTELLUNG. */
public class AufstellungRepository {
/** Ein Spieler mit seiner Rolle, noch ohne Datenbank-ID. */
public record Eintrag(int spielerId, Rolle rolle) {
}
        private static final String EINFUEGEN =
                "INSERT INTO aufstellung (spiel_id, spieler_id, rolle) VALUES (?, ?, ?)";
        /** Ein SQL-Aufruf pro Zeile. */
        public void einfuegen(Connection c, int spielId, int spielerId, Rolle rolle) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement(EINFUEGEN)) {
                ps.setInt(1, spielId);
                ps.setInt(2, spielerId);
                ps.setString(3, rolle.text());
                ps.executeUpdate();
            }
        }
        /** Ein Batch fuer alle Zeilen (wird nur in der Messung verglichen). */
        public void einfuegenGesammelt(Connection c, int spielId, List<Eintrag> eintraege) throws SQLException {
            try (PreparedStatement ps = c.prepareStatement(EINFUEGEN)) {
                for (Eintrag eintrag : eintraege) {
                    ps.setInt(1, spielId);
                    ps.setInt(2, eintrag.spielerId());
                    ps.setString(3, eintrag.rolle().text());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
        public List<AufstellungsZeile> zuSpiel(Connection c, int spielId) throws SQLException {
            String sql = "SELECT aufstellung_id, spiel_id, spieler_id, rolle, rote_karte "
                    + "FROM aufstellung WHERE spiel_id = ? ORDER BY aufstellung_id";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, spielId);
                List<AufstellungsZeile> liste = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        liste.add(new AufstellungsZeile(
                                rs.getInt("aufstellung_id"),
                                rs.getInt("spiel_id"),
                                rs.getInt("spieler_id"),
                                Rolle.vonText(rs.getString("rolle")),
                                rs.getBoolean("rote_karte")));
                    }
                }
                return liste;
            }
        }
        public void roteKarteSetzen(Connection c, int spielId, int spielerId) throws SQLException {
            String sql = "UPDATE aufstellung SET rote_karte = TRUE WHERE spiel_id = ? AND spieler_id = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, spielId);
                ps.setInt(2, spielerId);
                ps.executeUpdate();
            }
        }
}