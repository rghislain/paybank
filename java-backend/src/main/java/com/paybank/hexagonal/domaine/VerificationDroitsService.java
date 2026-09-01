package com.paybank.hexagonal.domaine;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Source unique de vérité pour vérifier les droits d'un rôle, côté serveur.
 *
 * Interroge exactement les mêmes tables ('utilisateurs' pour les actions génériques,
 * 'ressources' pour les modules) que celles affichées et modifiées via
 * GET/POST /api/droits (DroitsController), consommé par bilan.html et index.html.
 *
 * ⚠️ Ce service ne doit JAMAIS recevoir un rôle fourni par le client (header, body...).
 *    Le rôle doit toujours provenir de SecurityContextHolder (utilisateur authentifié).
 */
@Service
public class VerificationDroitsService {

    private static final List<String> ACTIONS_VALIDES =
            List.of("creer", "lire", "modifier", "supprimer", "sauvegarder", "imprimer", "envoyer");

    private static final List<String> RESSOURCES_VALIDES =
            List.of("clients", "paiements", "produits", "rapports_financiers", "parametres_systemes", "utilisateurs");

    private final JdbcTemplate jdbcTemplate;

    public VerificationDroitsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Vérifie qu'un rôle possède le droit demandé.
     *
     * @param role      le rôle réel de l'opérateur authentifié (jamais une valeur venant du client)
     * @param action    une action générique ("creer", "modifier", "supprimer", ...) ou null pour ne pas vérifier d'action
     * @param ressource une ressource/module ("clients", "produits", ...) ou null pour ne pas vérifier de ressource
     * @return true si le rôle a le droit (toutes les vérifications demandées sont positives)
     */
    public boolean aLeDroit(Role role, String action, String ressource) {
        if (role == null) {
            return false;
        }
        String roleUpper = role.name().toUpperCase();

        boolean actionOk = true;
        if (action != null && !action.isBlank()) {
            String actionLower = action.toLowerCase().trim();
            if (!ACTIONS_VALIDES.contains(actionLower)) {
                throw new IllegalArgumentException("Action inconnue : " + action);
            }
            actionOk = verifierAction(roleUpper, actionLower);
        }

        boolean ressourceOk = true;
        if (ressource != null && !ressource.isBlank()) {
            String ressourceLower = ressource.toLowerCase().trim();
            if (!RESSOURCES_VALIDES.contains(ressourceLower)) {
                throw new IllegalArgumentException("Ressource inconnue : " + ressource);
            }
            ressourceOk = verifierRessource(roleUpper, ressourceLower);
        }

        return actionOk && ressourceOk;
    }

    private boolean verifierAction(String roleUpper, String actionLower) {
        try {
            Map<String, Object> ligne = jdbcTemplate.queryForMap(
                    "SELECT " + actionLower + " FROM utilisateurs WHERE UPPER(role) = ? LIMIT 1",
                    roleUpper
            );
            Object valeur = ligne.get(actionLower);
            return Boolean.TRUE.equals(valeur);
        } catch (Exception e) {
            // Aucun utilisateur pour ce rôle, ou colonne absente : par sécurité, on refuse.
            return false;
        }
    }

    private boolean verifierRessource(String roleUpper, String ressourceLower) {
        try {
            Map<String, Object> ligne = jdbcTemplate.queryForMap(
                    "SELECT r." + ressourceLower + " FROM ressources r " +
                    "JOIN utilisateurs u ON r.utilisateurs_id = u.id " +
                    "WHERE UPPER(u.role) = ? LIMIT 1",
                    roleUpper
            );
            Object valeur = ligne.get(ressourceLower);
            return Boolean.TRUE.equals(valeur);
        } catch (Exception e) {
            // Aucune ligne de ressources pour ce rôle : par sécurité, on refuse.
            return false;
        }
    }
}