package com.paybank.hexagonal.adaptateurs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.paybank.hexagonal.DTO.ActivityDTO;
import com.paybank.hexagonal.domaine.RecentActivity;
import com.paybank.hexagonal.domaine.Transaction;
import com.paybank.hexagonal.domaine.TransactionDetail;
import com.paybank.hexagonal.entity.TransactionEntity;
import com.paybank.hexagonal.repository.SpringDataTransactionRepository;
import com.paybank.hexagonal.repository.TransactionRepository;

@Service
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final SpringDataTransactionRepository springDataTransactionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public TransactionRepositoryAdapter(SpringDataTransactionRepository springDataRepo) {
        this.springDataTransactionRepository = springDataRepo;
    }

    @Override
    public long countPending(UUID clientId) {
        return springDataTransactionRepository.countByClientIdAndStatus(clientId, "PENDING");
    }
    
    @Override
    public long countAllPending() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = 'PENDING'";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    /*
    @Override
    public List<Transaction> findTop5RecentActivities() {
        // Assurez-vous que les noms des colonnes SQL correspondent à votre table PostgreSQL 
        // (par exemple 'montant_centimes' en snake_case est très courant)
        String sql = "SELECT id, montant_centimes, date, reference, type FROM transactions ORDER BY date DESC LIMIT 5";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Transaction(
            rs.getString("id"),
            rs.getLong("montant_centimes"),
            // Conversion sécurisée du java.sql.Date vers LocalDate
            rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null,
            rs.getString("reference"),
            rs.getString("type")
        ));
    }
	*/
    
    @Override
    public List<Transaction> findTop5RecentActivities() {
        // Utilisation des vraies colonnes de la BDD (amount, description, status) 
        // avec des alias (AS) pour correspondre exactement à votre record Transaction
        String sql = """
            SELECT 
                id, 
                amount, 
                date, 
                description AS reference, 
                status AS type 
            FROM transactions 
            ORDER BY date DESC 
            LIMIT 5
        """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Transaction(
            rs.getString("id"),
            // Conversion du BigDecimal de la base (ex: 25.50) en long centimes (2550)
            rs.getBigDecimal("amount") != null ? rs.getBigDecimal("amount").movePointRight(2).longValue() : 0L,
            rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null,
            rs.getString("reference"), // Récupère l'alias de 'description'
            rs.getString("type")       // Récupère l'alias de 'status'
        ));
    }
    
    @Override
    public List<TransactionDetail> findTransactionsByDateRange(UUID clientId, LocalDate startDate, LocalDate endDate) {
        // 1. On appelle Spring Data pour récupérer les entités de la base de données
        List<TransactionEntity> entities = springDataTransactionRepository.findByClientIdAndDateBetween(clientId, startDate, endDate);
        
        // 2. On convertit chaque entité BDD en Record du Domaine (TransactionDetail)
        return entities.stream()
            .map(entity -> new TransactionDetail(
                entity.getId(),
                entity.getDate(),
                entity.getDescription(),
                entity.getAmount(),
                entity.isDebit(),
                entity.getReconciliationStatus()
            ))
            .toList();
    }
    

    @Override
    public BigDecimal getMonthlyRevenue(UUID clientId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        
        List<TransactionEntity> transactions = springDataTransactionRepository.findByClientIdAndDateBetween(clientId, start, end);
        
        return transactions.stream()
                .filter(t -> !t.isDebit()) // On ne compte que les revenus (crédits)
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    @Override
    public List<Transaction> findRecentActivities(UUID clientId, int limit) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return springDataTransactionRepository.findByClientIdOrderByDateDesc(clientId, pageable)
                .stream()
                .map(e -> new Transaction(
                    e.getId(), 
                    e.getDate().atStartOfDay(), 
                    e.getDescription(), 
                    e.getAmount(), 
                    e.isDebit()
                ))
                .toList();
    }
    */
    
    @Override
    public List<Transaction> findRecentActivities(UUID clientId, int limit) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return springDataTransactionRepository.findByClientIdOrderByDateDesc(clientId, pageable)
                .stream()
                .map(e -> new Transaction(
                    e.getId().toString(), // 1. String (conversion du UUID en String)
                    e.getAmount().movePointRight(2).longValue(), // 2. long (convertit BigDecimal en centimes si besoin, ou e.getMontantCentimes())
                    e.getDate(), // 3. LocalDate direct
                    e.getDescription(), // 4. String (utilisé comme référence)
                    e.isDebit() ? "DEBIT" : "CREDIT" // 5. String (transformation du booléen en texte)
                ))
                .toList();
    }

    @Override
    public BigDecimal calculateGlobalMonthlyRevenue() {
        // COALESCE garantit qu'on retourne 0 au lieu de null s'il n'y a pas de transaction ce mois-ci
        String sql = """
            SELECT COALESCE(SUM(amount), 0) 
            FROM transactions 
            WHERE EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM CURRENT_DATE)
              AND EXTRACT(YEAR FROM date) = EXTRACT(YEAR FROM CURRENT_DATE)
        """;
        
        BigDecimal revenue = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        
        // Sécurité supplémentaire pour éviter tout retour null
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}