package com.paybank.hexagonal.adaptateurs;

import com.paybank.hexagonal.entity.CompteEntity;
import com.paybank.hexagonal.repository.AccountRepository;
import com.paybank.hexagonal.repository.SpringDataAccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepositoryAdaptateur implements AccountRepository {

    private final SpringDataAccountRepository springDataRepo; // Ton repository JPA classique

    @Autowired
    private JdbcTemplate jdbcTemplate; // Injecte le template JDBC natif
    
    public AccountRepositoryAdaptateur(SpringDataAccountRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public BigDecimal findBalanceByClient(UUID clientId) {
        // Appel à la base de données via JPA et conversion si besoin vers le domaine
        return springDataRepo.findById(clientId)
                .map(CompteEntity::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    /*
	@Override
	public Optional<UUID> findFirstAccountId() {
		/*
		List<?> accounts = springDataRepo.findAll(); // ou List<VotreEntiteCompte>
	   		
	    if (accounts != null && !accounts.isEmpty()) {
	        // On récupère le premier élément et son ID
	        // (Assurez-vous que l'entité possède bien une méthode getId())
	        var firstAccount = accounts.get(0);
	    }
	    return Optional.empty();
	    */
		/*
		try {
            // Exécute une requête SQL brute directe
            String sql = "SELECT id FROM accounts LIMIT 1";
            UUID id = jdbcTemplate.queryForObject(sql, UUID.class);
            System.out.println(">>> SUCCÈS JDBC : ID trouvé en BDD = " + id);
            return Optional.ofNullable(id);
        } catch (Exception e) {
            System.out.println(">>> ERREUR JDBC : Aucun ID trouvé ou problème de connexion -> " + e.getMessage());
            return Optional.empty();
        }
	}
	*/
    /*
    @Override
    public BigDecimal getTotalBalance() {
        // COALESCE(..., 0) garantit que si la table est vide, 
        // cela retourne 0.00 au lieu de null ou d'une erreur
        String sql = "SELECT COALESCE(SUM(balance), 0) FROM accounts";
        
        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        
        System.out.println(">>> SQL GLOBAL : Solde total calculé = " + total);
        return total;
    }
    */
    
    @Override
    public BigDecimal getTotalBalance() {
        String sql = "SELECT COALESCE(SUM(balance), 0) FROM accounts";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

	@Override
	public Optional<UUID> findFirstAccountId() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}