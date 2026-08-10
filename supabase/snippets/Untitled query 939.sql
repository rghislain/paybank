DROP TABLE IF EXISTS utilisateurs CASCADE;

CREATE TABLE utilisateurs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50),
    actif BOOLEAN DEFAULT TRUE
);