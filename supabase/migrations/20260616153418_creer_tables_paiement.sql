-- =========================================================================
-- 1. TABLE : clients
-- =========================================================================
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    stripe_customer_id VARCHAR(100) UNIQUE, -- Pour lier avec l'ID client de Stripe
    cree_le TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================================
-- 2. TABLE : paiements
-- =========================================================================
CREATE TABLE paiements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    montant_centimes BIGINT NOT NULL,       -- Stripe exige des centimes (ex: 1500 pour 15.00€)
    devise VARCHAR(3) NOT NULL DEFAULT 'EUR',
    stripe_payment_intent_id VARCHAR(100) UNIQUE, -- L'ID de la transaction Stripe
    statut VARCHAR(50) NOT NULL,            -- ex: 'requires_payment_method', 'succeeded'
    cree_le TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Lien : Un paiement appartient à UN client
    CONSTRAINT fk_paiement_client 
        FOREIGN KEY (client_id) 
        REFERENCES clients(id) 
        ON DELETE CASCADE
);

-- =========================================================================
-- 3. TABLE : paiements_annules
-- =========================================================================
CREATE TABLE paiements_annules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    stripe_payment_intent_id VARCHAR(100) UNIQUE,
    montant_centimes BIGINT NOT NULL,
    devise VARCHAR(3) NOT NULL DEFAULT 'EUR',
    raison_annulation TEXT,
    annule_le TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Lien : Un paiement annulé appartient à UN client
    CONSTRAINT fk_annulation_client 
        FOREIGN KEY (client_id) 
        REFERENCES clients(id) 
        ON DELETE CASCADE
);

-- =========================================================================
-- OPTIMISATION : Index pour accélérer les recherches fréquentes
-- =========================================================================
CREATE INDEX idx_paiements_client ON paiements(client_id);
CREATE INDEX idx_paiements_annules_client ON paiements_annules(client_id);
