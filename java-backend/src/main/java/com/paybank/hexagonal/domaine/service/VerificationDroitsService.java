package com.paybank.hexagonal.domaine.service;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.paybank.hexagonal.domaine.Role;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.paybank.hexagonal.domaine.Role;

/**
 * Source unique de vérité pour vérifier les droits d'un rôle, côté serveur.
 *
 * Interroge désormais la table 'role_permissions' (role, ressource, action, granted),
 * la même que celle affichée et modifiée via GET/POST /api/droits (DroitsControleur),
 * consommée par bilan.html.
 *
 * Ce service ne doit JAMAIS recevoir un rôle fourni par le client (header, body...).
 * Le rôle doit toujours provenir de SecurityContextHolder (utilisateur authentifié).
 *
 * Note de migration : les anciennes tables 'utilisateurs'/'ressources' interrogées
 * précédemment ici ne sont plus mises à jour par la matrice de droits (qui écrit
 * désormais dans role_permissions). Ce service devait être migré en conséquence,
 * sans quoi les modifications faites via la matrice n'auraient plus aucun effet
 * sur les autorisations réellement appliquées.
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
     * @param action    une action ("creer", "modifier", "supprimer", ...) ou null pour ne pas filtrer dessus
     * @param ressource une ressource/module ("clients", "produits", ...) ou null pour ne pas filtrer dessus
     * @return true si un enregistrement granted=true existe dans role_permissions pour ce rôle
     *         et les filtres fournis. Si action ET ressource sont fournis, c'est le triplet exact
     *         qui est vérifié. Si un seul des deux est fourni, on vérifie qu'AU MOINS UNE ligne
     *         correspondante est accordée (équivalent au comportement précédent sur les colonnes
     *         globales au rôle).
     */
    public boolean aLeDroit(Role role, String action, String ressource) {
        if (role == null) {
            return false;
        }
        String roleUpper = role.name().toUpperCase();

        boolean aucunFiltre = (action == null || action.isBlank()) && (ressource == null || ressource.isBlank());
        if (aucunFiltre) {
            return true;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT EXISTS (SELECT 1 FROM role_permissions WHERE role = ? AND granted = true"
        );
        List<Object> parametres = new ArrayList<>();
        parametres.add(roleUpper);

        if (action != null && !action.isBlank()) {
            String actionLower = action.toLowerCase().trim();
            if (!ACTIONS_VALIDES.contains(actionLower)) {
                throw new IllegalArgumentException("Action inconnue : " + action);
            }
            sql.append(" AND action = ?");
            parametres.add(actionLower);
        }

        if (ressource != null && !ressource.isBlank()) {
            String ressourceLower = ressource.toLowerCase().trim();
            if (!RESSOURCES_VALIDES.contains(ressourceLower)) {
                throw new IllegalArgumentException("Ressource inconnue : " + ressource);
            }
            sql.append(" AND ressource = ?");
            parametres.add(ressourceLower);
        }

        sql.append(")");

        Boolean existe = jdbcTemplate.queryForObject(sql.toString(), Boolean.class, parametres.toArray());
        return Boolean.TRUE.equals(existe);
    }
}
