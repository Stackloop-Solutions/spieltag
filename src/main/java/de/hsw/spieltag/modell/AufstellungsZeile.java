package de.hsw.spieltag.modell;

public record AufstellungsZeile(int id, int spielId, int spielerId, Rolle rolle, boolean roteKarte) {
}