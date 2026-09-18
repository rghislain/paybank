package com.paybank.hexagonal.sortie.port.adaptateur;

import com.paybank.hexagonal.entite.CompteEntity;
import com.paybank.hexagonal.jpaRepository.AccountRepository;
import com.paybank.hexagonal.jpaRepository.SpringDataAccountRepository;

import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccountRepositoryAdaptateur implements AccountRepository {

    private final SpringDataAccountRepository springDataRepo; //repository JPA classique

    @Autowired
    private JdbcTemplate jdbcTemplate; //Injecte le template JDBC natif
    
    public AccountRepositoryAdaptateur(SpringDataAccountRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public BigDecimal findBalanceByClient(UUID clientId) {
        //Appel à la base de données via JPA et conversion si besoin vers le domaine
        return springDataRepo.findById(clientId)
                .map(CompteEntity::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getTotalBalance() {
        String sql = "SELECT COALESCE(SUM(balance), 0) FROM accounts";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class);
    }

	@Override
	public Optional<UUID> findFirstAccountId() {		
		return Optional.empty();
	}
}