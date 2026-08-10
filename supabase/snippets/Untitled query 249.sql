CREATE TABLE produits (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prix_centimes BIGINT NOT NULL,
    stripe_product_id VARCHAR(255),
    stripe_price_id VARCHAR(255)
);