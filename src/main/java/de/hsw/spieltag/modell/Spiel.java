package de.hsw.spieltag.modell;
import java.time.LocalDate;
public record Spiel(int id, String gegner, LocalDate datum, SpielStatus status,
                    Integer toreEigen, Integer toreGegner) {
}