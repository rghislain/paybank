package com.paybank.hexagonal.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public AccountInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [INIT] Début de l'initialisation de la table accounts...");

        try {
            // 1. Ajouter la colonne client_id à la table accounts si elle n'existe pas encore
            jdbcTemplate.execute("ALTER TABLE accounts ADD COLUMN IF NOT EXISTS client_id UUID;");
            System.out.println(">>> [INIT] Colonne client_id vérifiée/ajoutée avec succès.");

            // 2. Vider la table accounts pour éviter les doublons ou données obsolètes à chaque redémarrage
            jdbcTemplate.execute("DELETE FROM accounts;");

            // 3. Remplir la table accounts en associant chaque client à ses paiements
            // On calcule la somme de 'montant_centimes' de la table 'paiements' convertie en euros (divisée par 100.0)
            String populateSql = """
                INSERT INTO accounts (id, client_id, balance)
                SELECT 
                    gen_random_uuid(), 
                    c.id, 
                    COALESCE(SUM(p.montant_centimes), 0) / 100.0
                FROM clients c
                LEFT JOIN paiements p ON p.client_id = c.id
                GROUP BY c.id;
            """;

            jdbcTemplate.execute(populateSql);
            System.out.println(">>> [INIT] Table accounts remplie avec succès (liée aux clients et calculée depuis les paiements).");

        } catch (Exception e) {
            System.err.println(">>> [ERREUR INIT] Échec lors de l'initialisation de la table accounts : " + e.getMessage());
            e.printStackTrace();
        }
    }
}