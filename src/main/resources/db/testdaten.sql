-- Testdaten: 16 Spieler (2 Torhueter, 1 gesperrt), 6 Spiele, Aufstellungen von 2 Spielen.
-- Die IDs entstehen in der Reihenfolge der Zeilen (1, 2, 3, ...), nur auf einer frischen Datenbank ausfuehren.
INSERT INTO spieler (name, position, trikotnummer, gesperrt_bis) VALUES
                                                                     ('Lukas Brandt',  'Torwart',    1,  NULL),
                                                                     ('Felix Wagner',  'Torwart',    12, NULL),
                                                                     ('Jan Becker',    'Abwehr',     2,  NULL),
                                                                     ('Tom Hoffmann',  'Abwehr',     3,  NULL),
                                                                     ('Nico Schulz',   'Abwehr',     4,  NULL),
                                                                     ('Ben Koch',      'Abwehr',     5,  NULL),
                                                                     ('Paul Richter',  'Abwehr',     15, NULL),
                                                                     ('Max Neumann',   'Mittelfeld', 6,  NULL),
                                                                     ('Leon Fischer',  'Mittelfeld', 8,  NULL),
                                                                     ('David Klein',   'Mittelfeld', 10, NULL),
                                                                     ('Erik Wolf',     'Mittelfeld', 14, NULL),
                                                                     ('Simon Braun',   'Mittelfeld', 16, NULL),
                                                                     ('Jonas Keller',  'Sturm',      9,  NULL),
                                                                     ('Tim Lang',      'Sturm',      11, NULL),
                                                                     ('Marc Vogel',    'Sturm',      17, NULL),
                                                                     ('Hannes Roth',   'Sturm',      19, DATE '2026-12-20');
INSERT INTO spiel (gegner, spieldatum, status, tore_eigen, tore_gegner) VALUES
                                                                            ('FC Nord',       DATE '2026-09-06', 'abgeschlossen', 2,    1),
                                                                            ('SV Ost',        DATE '2026-11-29', 'aufgestellt',   NULL, NULL),
                                                                            ('TSV West',      DATE '2026-12-06', 'geplant',       NULL, NULL),
                                                                            ('Rot-Weiss Sued',DATE '2027-01-17', 'geplant',       NULL, NULL),
                                                                            ('Blau-Gelb Mitte',DATE '2027-01-31','geplant',       NULL, NULL),
                                                                            ('FC Hafen',      DATE '2027-02-14', 'geplant',       NULL, NULL);
-- Spiel 1 (abgeschlossen) und Spiel 2 (aufgestellt): gleiche Aufstellung
-- Start: 1 (Torwart), 3, 4, 5, 6 (Abwehr), 8, 9, 10, 11 (Mittelfeld), 13, 14 (Sturm)
-- Bank:  2, 7, 12
INSERT INTO aufstellung (spiel_id, spieler_id, rolle) VALUES
                                                          (1, 1, 'Start'), (1, 3, 'Start'), (1, 4, 'Start'), (1, 5, 'Start'), (1, 6, 'Start'),
                                                          (1, 8, 'Start'), (1, 9, 'Start'), (1, 10, 'Start'), (1, 11, 'Start'),
                                                          (1, 13, 'Start'), (1, 14, 'Start'),
                                                          (1, 2, 'Bank'), (1, 7, 'Bank'), (1, 12, 'Bank'),
                                                          (2, 1, 'Start'), (2, 3, 'Start'), (2, 4, 'Start'), (2, 5, 'Start'), (2, 6, 'Start'),
                                                          (2, 8, 'Start'), (2, 9, 'Start'), (2, 10, 'Start'), (2, 11, 'Start'),
                                                          (2, 13, 'Start'), (2, 14, 'Start'),
                                                          (2, 2, 'Bank'), (2, 7, 'Bank'), (2, 12, 'Bank');
