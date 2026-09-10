package com.paybank.hexagonal.adaptateur;

import com.paybank.hexagonal.domaine.Client;
import com.paybank.hexagonal.port.ClientSPI;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SupabaseClientAdaptateur implements ClientSPI {

    private final JdbcTemplate jdbcTemplate;

    public SupabaseClientAdaptateur(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void sauvegarder(Client client) {
        String sql = "INSERT INTO clients (id, nom, email, stripe_customer_id) VALUES (?::uuid, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET nom = EXCLUDED.nom, email = EXCLUDED.email, stripe_customer_id = EXCLUDED.stripe_customer_id";
        jdbcTemplate.update(sql, client.getId().toString(), client.getNom(), client.getEmail(), client.getStripeCustomerId());
    }

    @Override
    public Optional<Client> trouverParId(UUID id) {
        String sql = "SELECT id, nom, email, stripe_customer_id FROM clients WHERE id = ?::uuid";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Client(
                UUID.fromString(rs.getString("id")),
                rs.getString("nom"),
                rs.getString("email"),
                rs.getString("stripe_customer_id")
        ), id.toString()).stream().findFirst();
    }

    @Override
    public void supprimer(UUID id) {
        String sql = "DELETE FROM clients WHERE id = ?::uuid";
        jdbcTemplate.update(sql, id.toString());
    }

    @Override
	public List<Client> listerTousLesClients() {
	    String sql = "SELECT id, nom, email, stripe_customer_id FROM clients";
	    return jdbcTemplate.query(sql, (rs, rowNum) -> new Client(
	            UUID.fromString(rs.getString("id")),
	            rs.getString("nom"),
	            rs.getString("email"),
	            rs.getString("stripe_customer_id")
	    ));
	}
	
}