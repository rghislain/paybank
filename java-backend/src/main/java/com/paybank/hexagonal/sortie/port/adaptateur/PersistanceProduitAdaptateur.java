package com.paybank.hexagonal.sortie.port.adaptateur;

import com.paybank.hexagonal.domaine.model.Produit;
import com.paybank.hexagonal.sortie.port.ProduitSPI;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PersistanceProduitAdaptateur implements ProduitSPI {

    private final JdbcTemplate jdbcTemplate;

    public PersistanceProduitAdaptateur(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void sauvegarder(Produit produit) {
        String sql = "INSERT INTO produits (id, nom, prix_centimes, stripe_product_id, stripe_price_id) VALUES (?::uuid, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom, prix_centimes = EXCLUDED.prix_centimes, " +
                     "stripe_product_id = EXCLUDED.stripe_product_id, stripe_price_id = EXCLUDED.stripe_price_id";
        jdbcTemplate.update(sql, produit.getId().toString(), produit.getNom(), produit.getPrixCentimes(), 
                produit.getStripeProductId(), produit.getStripePriceId());
    }

    @Override
    public Optional<Produit> trouverParId(UUID id) {
        String sql = "SELECT id, nom, prix_centimes, stripe_product_id, stripe_price_id FROM produits WHERE id = ?::uuid";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Produit(
                UUID.fromString(rs.getString("id")),
                rs.getString("nom"),
                rs.getLong("prix_centimes"),
                rs.getString("stripe_product_id"),
                rs.getString("stripe_price_id")
        ), id.toString()).stream().findFirst();
    }

    @Override
    public void supprimer(UUID id) {
        String sql = "DELETE FROM produits WHERE id = ?::uuid";
        jdbcTemplate.update(sql, id.toString());
    }

	@Override
	public List<Produit> listerTous() {
		String sql = "SELECT id, nom, prix_centimes, stripe_product_id, stripe_price_id FROM produits";
	    return jdbcTemplate.query(sql, (rs, rowNum) -> new Produit(
	            UUID.fromString(rs.getString("id")),
	            rs.getString("nom"),
	            rs.getLong("prix_centimes"),
	            rs.getString("stripe_product_id"),
	            rs.getString("stripe_price_id")
	    ));
	}
}