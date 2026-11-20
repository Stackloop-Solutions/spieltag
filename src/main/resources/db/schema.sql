-- Schema "Spieltag und Aufstellung": 3 Tabellen, 2 Fremdschluessel.
-- Das Skript darf mehrfach laufen (IF NOT EXISTS), es loescht keine Daten.
CREATE TABLE IF NOT EXISTS spieler (
                                       spieler_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       name         VARCHAR(100) NOT NULL,
    position     VARCHAR(20)  NOT NULL,
    trikotnummer INT          NOT NULL,
    gesperrt_bis DATE,
    CONSTRAINT uq_spieler_trikotnummer UNIQUE (trikotnummer),
    CONSTRAINT ck_spieler_name         CHECK (CHAR_LENGTH(name) BETWEEN 1 AND 100),
    CONSTRAINT ck_spieler_position     CHECK (position IN ('Torwart', 'Abwehr', 'Mittelfeld', 'Sturm')),
    CONSTRAINT ck_spieler_trikotnummer CHECK (trikotnummer BETWEEN 1 AND 99)
    );
CREATE TABLE IF NOT EXISTS spiel (
                                     spiel_id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     gegner      VARCHAR(100) NOT NULL,
    spieldatum  DATE         NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'geplant',
    tore_eigen  INT,
    tore_gegner INT,
    CONSTRAINT ck_spiel_status CHECK (status IN ('geplant', 'aufgestellt', 'abgeschlossen')),
    CONSTRAINT ck_spiel_tore   CHECK ((tore_eigen IS NULL OR tore_eigen >= 0)
    AND (tore_gegner IS NULL OR tore_gegner >= 0))
    );
CREATE TABLE IF NOT EXISTS aufstellung (
                                           aufstellung_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                           spiel_id       INT         NOT NULL,
                                           spieler_id     INT         NOT NULL,
                                           rolle          VARCHAR(10) NOT NULL,
    rote_karte     BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_aufstellung_spiel   FOREIGN KEY (spiel_id)   REFERENCES spiel (spiel_id),
    CONSTRAINT fk_aufstellung_spieler FOREIGN KEY (spieler_id) REFERENCES spieler (spieler_id),
    CONSTRAINT uq_aufstellung_spiel_spieler UNIQUE (spiel_id, spieler_id),
    CONSTRAINT ck_aufstellung_rolle   CHECK (rolle IN ('Start', 'Bank'))
    );