package de.hsw.spieltag.daten;

/** Technischer Fehler beim Zugriff auf die Datenbank (keine fachliche Ablehnung). */
public class DatenbankException extends RuntimeException {

    public DatenbankException(String nachricht, Throwable ursache) {
        super(nachricht, ursache);
    }
}