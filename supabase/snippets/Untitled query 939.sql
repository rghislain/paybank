CREATE TABLE passwords (
    id VARCHAR(50) PRIMARY KEY, -- ou l'équivalent selon la longueur de vos identifiants textuels
    role VARCHAR(100) UNIQUE NOT NULL, -- ex: 'ADMIN', 'MANAGER', 'EMPLOYE'
    password_hash VARCHAR(255) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) REFERENCES utilisateurs(id) ON DELETE SET NULL -- Stocke l'identifiant VARCHAR de l'administrateur
);