CREATE TABLE embedded_postgres_lifecycle_probe (
    id INTEGER PRIMARY KEY,
    value VARCHAR(64) NOT NULL
);
INSERT INTO embedded_postgres_lifecycle_probe (id, value) VALUES (1, 'initialized');
