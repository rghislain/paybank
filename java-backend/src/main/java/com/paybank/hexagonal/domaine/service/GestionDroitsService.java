package com.paybank.hexagonal.domaine.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.paybank.hexagonal.domaine.Role;
import jakarta.transaction.Transactional;

/**
 * Gestion des droits d'accès sous forme de triplets indépendants
 * (role, ressource, action) -> granted.
 *
 * IMPORTANT : ces deux listes doivent rester rigoureusement synchronisées
 * avec `actionsList` et `ressourcesList` dans bilan.html, sans quoi une
 * case de la matrice pourra être cochée côté écran mais jamais persistée.
 */
@Service
public class GestionDroitsService {

    private static final Set<String> ACTIONS_VALIDES = Set.of(
            "creer", "lire", "modifier", "supprimer", "sauvegarder", "imprimer", "envoyer"
    );

    private static final Set<String> RESSOURCES_VALIDES = Set.of(
            "clients", "paiements", "produits", "utilisateurs", "rapports_financiers", "parametres_systemes"
    );

    private final JdbcTemplate jdbcTemplate;

    public GestionDroitsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Retourne la liste complète des droits, un triplet par ligne :
     * [{ role, ressource, action, granted }, ...]
     *
     * C'est le format attendu par chargerMatriceDroits() côté front,
     * qui cible ensuite chaque case précisément via ses data-attributes.
     */
    public List<Map<String, Object>> obtenirToutesLesPermissions() {
        String sql = "SELECT role, ressource, action, granted " +
                     "FROM role_permissions " +
                     "ORDER BY role, ressource, action";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Retourne l'état actuel (granted) d'un triplet précis, ou null si aucune
     * ligne n'existe encore pour cette combinaison. Utile notamment pour
     * journaliser l'ancienne valeur avant une modification (audit log).
     */
    public Boolean obtenirPermission(String role, String ressource, String action) {
        String sql = "SELECT granted FROM role_permissions WHERE role = ? AND ressource = ? AND action = ?";
        List<Boolean> resultat = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getBoolean("granted"),
                role.toUpperCase().trim(),
                ressource.toLowerCase().trim(),
                action.toLowerCase().trim()
        );
        return resultat.isEmpty() ? null : resultat.get(0);
    }

    /**
     * Crée ou met à jour un droit précis pour un rôle donné.
     * Un seul triplet (role, ressource, action) est concerné par appel :
     * cela correspond exactement à une case cochée/décochée dans la matrice.
     */
    @Transactional
    public void mettreAJourPermissionParRole(String role, String ressource, String action, boolean isGranted) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Le rôle est obligatoire.");
        }
        if (ressource == null || ressource.isBlank()) {
            throw new IllegalArgumentException("La ressource est obligatoire.");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("L'action est obligatoire.");
        }

        String roleNormalise = role.toUpperCase().trim();
        String ressourceNormalisee = ressource.toLowerCase().trim();
        String actionNormalisee = action.toLowerCase().trim();

        if (!RESSOURCES_VALIDES.contains(ressourceNormalisee)) {
            throw new IllegalArgumentException("Ressource inconnue : " + ressource);
        }
        if (!ACTIONS_VALIDES.contains(actionNormalisee)) {
            throw new IllegalArgumentException("Action inconnue : " + action);
        }

        String sql = "INSERT INTO role_permissions (id, role, ressource, action, granted, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT ON CONSTRAINT uq_role_permissions_triplet " +
                     "DO UPDATE SET granted = EXCLUDED.granted, updated_at = EXCLUDED.updated_at";

        jdbcTemplate.update(
                sql,
                UUID.randomUUID(),
                roleNormalise,
                ressourceNormalisee,
                actionNormalisee,
                isGranted,
                Timestamp.from(Instant.now())
        );
    }

    /**
     * Applique la même valeur à toutes les actions d'une ressource, pour un rôle donné.
     * Pratique pour initialiser un rôle ou pour un bouton "tout cocher / tout décocher"
     * sur une colonne-ressource entière. Remplace l'ancienne méthode du même nom qui
     * s'appuyait sur les entités UtilisateurEntity / RessourcesEntity (modèle abandonné).
     */
    @Transactional
    public void mettreAJourDroitsParRole(Role role, String nomRessource, boolean valeur) {
        if (nomRessource == null || nomRessource.isBlank()) {
            throw new IllegalArgumentException("La ressource est obligatoire.");
        }

        String ressourceNormalisee = nomRessource.toLowerCase().trim();
        if (!RESSOURCES_VALIDES.contains(ressourceNormalisee)) {
            throw new IllegalArgumentException("Ressource inconnue : " + nomRessource);
        }

        for (String action : ACTIONS_VALIDES) {
            mettreAJourPermissionParRole(role.name(), ressourceNormalisee, action, valeur);
        }
    }

    /**
     * Symétrique de mettreAJourDroitsParRole : applique une même action à
     * TOUTES les ressources pour un rôle donné (au lieu d'une ressource à
     * toutes les actions). Utile pour un appelant qui ne raisonne que par
     * action globale (ex. UtilisateurSPI.basculerDroitPourRole), sans notion
     * de ressource précise.
     */
    @Transactional
    public void mettreAJourActionPourToutesLesRessources(Role role, String nomAction, boolean valeur) {
        if (nomAction == null || nomAction.isBlank()) {
            throw new IllegalArgumentException("L'action est obligatoire.");
        }

        String actionNormalisee = nomAction.toLowerCase().trim();
        if (!ACTIONS_VALIDES.contains(actionNormalisee)) {
            throw new IllegalArgumentException("Action inconnue : " + nomAction);
        }

        for (String ressource : RESSOURCES_VALIDES) {
            mettreAJourPermissionParRole(role.name(), ressource, actionNormalisee, valeur);
        }
    }
}