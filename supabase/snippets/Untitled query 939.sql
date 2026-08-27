-- 1. Suppression propre de l'ancienne table
DROP TABLE IF EXISTS ressources CASCADE;

-- 2. Création avec un ID de type UUID (pour correspondre à Hibernate)
CREATE TABLE ressources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateurs_id VARCHAR(50) NOT NULL UNIQUE,
    clients BOOLEAN DEFAULT FALSE,
    paiements BOOLEAN DEFAULT FALSE,
    produits BOOLEAN DEFAULT FALSE,
    rapports_financiers BOOLEAN DEFAULT FALSE,
    parametres_systemes BOOLEAN DEFAULT FALSE,
    creer BOOLEAN DEFAULT FALSE,
    lire BOOLEAN DEFAULT FALSE,
    modifier BOOLEAN DEFAULT FALSE,
    supprimer BOOLEAN DEFAULT FALSE,
    sauvegarder BOOLEAN DEFAULT FALSE,
    imprimer BOOLEAN DEFAULT FALSE
);