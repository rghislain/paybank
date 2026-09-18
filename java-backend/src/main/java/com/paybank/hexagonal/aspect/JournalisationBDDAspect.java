package com.paybank.hexagonal.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut; // ➕ Ajouté
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.paybank.hexagonal.domaine.model.Role;
import com.paybank.hexagonal.domaine.service.OperateurCourantService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Order(2)
public class JournalisationBDDAspect {

    private final JdbcTemplate jdbcTemplate;
    
    private final OperateurCourantService operateurCourantService;

    public JournalisationBDDAspect(JdbcTemplate jdbcTemplate, OperateurCourantService operateurCourantService) {
        this.jdbcTemplate = jdbcTemplate;
		this.operateurCourantService = operateurCourantService;
    }

    /**
     * Définition propre du Pointcut sous forme de méthode
     */
    @Pointcut("execution(* com.paybank.hexagonal.domaine.ServiceGestionPaiement.*(..))")
    public void interceptionServicePaiement() {}

    /**
     * Cas 1 : Succès
     */    
    @AfterReturning(pointcut = "interceptionServicePaiement()", returning = "resultat")
    public void enregistrerSuccesBdd(JoinPoint joinPoint, Object resultat) {
        String methode = joinPoint.getSignature().getName();
        String roleOperateur = this.extraireRoleSecurise(); 
        String arguments = Arrays.toString(joinPoint.getArgs());

        //1. Log global de l'action
        String detailsJson = "{\"arguments\":\"" + nettoyerPourJson(arguments) + "\"}";
        String sqlGlobal = "INSERT INTO journalisation (operateur_role, action, statut, details) VALUES (?, ?, ?, ?::jsonb)";
        jdbcTemplate.update(sqlGlobal, roleOperateur, methode, "SUCCES", detailsJson);

        //2. S'IL S'AGIT DU RAPPROCHEMENT : On extrait et journalise CHAQUE anomalie individuellement
        if ("executerRapprochementDepuisSources".equals(methode) && resultat instanceof List) {
            List<?> extraits = (List<?>) resultat;            
            for (Object obj : extraits) {
                if (obj instanceof com.paybank.hexagonal.domaine.model.MatchResult) {
                    com.paybank.hexagonal.domaine.model.MatchResult res = (com.paybank.hexagonal.domaine.model.MatchResult) obj;                    
                    //On ne journalise en BDD que les anomalies (on ignore les MATCH parfaits pour ne pas surcharger)
                    if (!"MATCH".equals(res.statut())) {
                        String detailAnomalie = String.format(
                            "{\"type\":\"%s\",\"montant\":%s,\"details\":\"Alerte rapprochement\"}",
                            res.statut(),
                            res.tCompta() != null ? res.tCompta().montantCentimes() : res.tBanque().montantCentimes()
                        );                    
                        //Insertion d'une ligne d'action spécifique par écart détecté
                        jdbcTemplate.update(
                            "INSERT INTO journalisation (operateur_role, action, statut, details) VALUES (?, ?, ?, ?::jsonb)",
                            "SYSTEME", 
                            "ECART_" + res.statut(), //Ex: 'ECART_MANQUANT' ou 'ECART_INCONNU'
                            "WARN", 
                            detailAnomalie
                        );
                    }
                }
            }
        }
    }

    /**
     * Cas 2 : Échec
     */
    @AfterThrowing(pointcut = "interceptionServicePaiement()", throwing = "exception")
    public void enregistrerEchecBdd(JoinPoint joinPoint, Throwable exception) {
        String methode = joinPoint.getSignature().getName();
        String roleOperateur = extraireRoleSecurise(); 
        String arguments = Arrays.toString(joinPoint.getArgs());
        String detailsJson = "{\"arguments\":\"" + nettoyerPourJson(arguments) 
                           + "\",\"erreur\":\"" + nettoyerPourJson(exception.getMessage()) + "\"}";
        System.err.println("🚨 [AUDIT INTERNE] Log d'échec pour " + methode);
        String sql = "INSERT INTO journalisation (operateur_role, action, statut, details) VALUES (?, ?, ?, ?::jsonb)";
        jdbcTemplate.update(sql, roleOperateur, methode, "ECHEC", detailsJson);
    }

    private String extraireRoleDepuisRequete() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String role = request.getHeader("X-Auth-Role");
                return (role != null) ? role : "SYSTEME";
            }
        } catch (Exception e) {
            return "INTERNAL_TASK";
        }
        return "INCONNU";
    }
    
    private String extraireRoleSecurise() {
        try {
            Role role = operateurCourantService.getRoleConnecte();
            return role != null ? role.name() : "INCONNU";
        } catch (Exception e) {
            return "SYSTEME"; // Si exécuté dans un thread sans contexte HTTP/User (ex: tâche planifiée)
        }
    }
    
    private String nettoyerPourJson(String texte) {
        if (texte == null) return "";
        return texte.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", " ")
                    .replace("\r", "");
    }
}