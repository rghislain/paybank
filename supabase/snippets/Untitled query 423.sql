CREATE TABLE journalisation (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    niveau_log VARCHAR(10) NOT NULL, -- 'INFO', 'WARN', 'ERROR', 'DEBUG'
    action VARCHAR(100) NOT NULL,    -- Ex: 'CREATION_PAIEMENT', 'ECHEC_STRIPE', 'ANNULATION_REUSSIE'
    message TEXT NOT NULL,           -- Description ou payload d'erreur
    entite_concernee VARCHAR(50),    -- Ex: 'PAIEMENT', 'CLIENT'
    entite_id UUID,                  -- ID lié (optionnel)
    cree_le TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);