package com.paybank.hexagonal.domaine;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.paybank.hexagonal.entity.RessourcesEntity;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.repository.RessourcesRepository;
import com.paybank.hexagonal.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;

@Service
public class GestionDroitsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RessourcesRepository ressourcesRepository;
    
    private JdbcTemplate jdbcTemplate;
    
    public GestionDroitsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void mettreAJourDroitsParRole(Role role, String nomRessource, boolean valeur) {
        List<UtilisateurEntity> utilisateurs = utilisateurRepository.findByRole(role);

        for (UtilisateurEntity utilisateur : utilisateurs) {
            RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(utilisateur.getId())
                .orElseGet(() -> {
                    RessourcesEntity nouvelleRessource = new RessourcesEntity();
                    nouvelleRessource.setId(UUID.randomUUID());
                    nouvelleRessource.setUtilisateursId(utilisateur.getId());
                    return nouvelleRessource;
                });

            // Mise à jour de la colonne correspondante
            switch (nomRessource.toLowerCase()) {
                case "clients":
                    ressource.setClients(valeur);
                    break;
                case "paiements":
                    ressource.setPaiements(valeur);
                    break;
                // Ajoutez les autres ressources ici...
            }
            ressourcesRepository.save(ressource);
        }
    }

    /*
    @Transactional
    public void mettreAJourPermissionParRole(String roleCibleStr, String ressourceNom, String action, boolean isGranted) {
        // Conversion du string du rôle en enum Role (ajustez selon votre enum)
        Role roleCible = Role.valueOf(roleCibleStr.toUpperCase());
        
        // Récupérer tous les utilisateurs ayant ce rôle
        List<Utilisateur> utilisateurs = utilisateurRepository.findByRole(roleCible);

        for (Utilisateur utilisateur : utilisateurs) {
            // Chercher ou créer la ligne de ressources liée à cet utilisateur
            RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(utilisateur.getId())
                .orElseGet(() -> {
                    RessourcesEntity nouvelleRessource = new RessourcesEntity();
                    nouvelleRessource.setId(UUID.randomUUID());
                    nouvelleRessource.setUtilisateursId(utilisateur.getId());
                    return nouvelleRessource;
                });

            // Mettre à jour la colonne correspondante selon la ressource transmise
            switch (ressourceNom.toLowerCase()) {
                case "clients":
                    ressource.setClients(String.valueOf(isGranted));
                    break;
                case "paiements":
                    ressource.setPaiements(String.valueOf(isGranted));
                    break;
                case "produits":
                    ressource.setProduits(String.valueOf(isGranted));
                    break;
                case "rapports_financiers":
                case "rapports":
                    ressource.setRapportsFinanciers(String.valueOf(isGranted));
                    break;
                case "parametres_systemes":
                case "parametres":
                    ressource.setParametresSystemes(String.valueOf(isGranted));
                    break;
                default:
                    throw new IllegalArgumentException("Ressource inconnue : " + ressourceNom);
            }

            ressourcesRepository.save(ressource);
        }
    }
    */
    
    /*
    @Transactional
    public void mettreAJourPermissionParRole(String roleCibleStr, String ressourceNom, String action, boolean isGranted) {
        if (roleCibleStr == null) {
            throw new IllegalArgumentException("Le rôle cible ne peut pas être null.");
        }

        // Nettoyage de la chaîne (enlève "ROLE_" si présent et met en majuscules)
        String cleanRole = roleCibleStr.toUpperCase().replace("ROLE_", "");
        Role roleCible = Role.valueOf(cleanRole);
        
        List<UtilisateurEntity> utilisateurs = utilisateurRepository.findByRole(roleCible);
        System.out.println("DEBUG - Utilisateurs trouvés pour le rôle " + roleCible + " : " + utilisateurs.size());

        for (UtilisateurEntity utilisateur : utilisateurs) {
            RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(utilisateur.getId())
                .orElseGet(() -> {
                    RessourcesEntity nouvelleRessource = new RessourcesEntity();
                    nouvelleRessource.setId(UUID.randomUUID());
                    nouvelleRessource.setUtilisateursId(utilisateur.getId());
                    return nouvelleRessource;
                });

            switch (ressourceNom.toLowerCase()) {
                case "clients":
                    ressource.setClients(String.valueOf(isGranted));
                    break;
                case "paiements":
                    ressource.setPaiements(String.valueOf(isGranted));
                    break;
                case "produits":
                    ressource.setProduits(String.valueOf(isGranted));
                    break;
                case "rapports_financiers":
                case "rapports":
                    ressource.setRapportsFinanciers(String.valueOf(isGranted));
                    break;
                case "parametres_systemes":
                case "parametres":
                    ressource.setParametresSystemes(String.valueOf(isGranted));
                    break;
                default:
                    throw new IllegalArgumentException("Ressource inconnue : " + ressourceNom);
            }

            ressourcesRepository.save(ressource);
        }
    }
    */
    
    /*
    @Transactional
    public void mettreAJourPermissionParRole(String roleCibleStr, String ressourceNom, String action, boolean isGranted) {
        if (roleCibleStr == null) {
            throw new IllegalArgumentException("Le rôle cible ne peut pas être null.");
        }

        String cleanRole = roleCibleStr.toUpperCase().replace("ROLE_", "");
        Role roleCible = Role.valueOf(cleanRole);
        
        // Récupérer tous les utilisateurs ayant ce rôle
        List<UtilisateurEntity> utilisateurs = utilisateurRepository.findByRole(roleCible);
        System.out.println("DEBUG - Utilisateurs trouvés pour le rôle " + roleCible + " : " + utilisateurs.size());

        for (UtilisateurEntity utilisateur : utilisateurs) {
            
            // 1. Mise à jour de l'ACTION dans la table utilisateurs (ex: modifier, supprimer, creer...)
            if (action != null) {
                switch (action.toLowerCase()) {
                    case "modifier":
                        utilisateur.setModifier(isGranted);
                        break;
                    case "lire":
                    case "voir":
                        utilisateur.setLire(isGranted);
                        break;
                    case "creer":
                        utilisateur.setCreer(isGranted);
                        break;
                    case "supprimer":
                        utilisateur.setSupprimer(isGranted);
                        break;
                    case "imprimer":
                        utilisateur.setImprimer(isGranted);
                        break;
                    case "sauvegarder":
                        utilisateur.setSauvegarder(isGranted);
                        break;
                    default:
                        System.out.println("Action non reconnue pour utilisateurs : " + action);
                }
                utilisateurRepository.save(utilisateur);
            }

            // 2. Mise à jour de la RESSOURCE dans la table ressources (ex: paiements, clients, produits...)
            if (ressourceNom != null) {
                RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(utilisateur.getId().toString())
                    .orElseGet(() -> {
                        RessourcesEntity nouvelleRessource = new RessourcesEntity();
                        nouvelleRessource.setId(UUID.randomUUID());
                        nouvelleRessource.setUtilisateursId(utilisateur.getId().toString());
                        return nouvelleRessource;
                    });

                switch (ressourceNom.toLowerCase()) {
                    case "clients":
                        ressource.setClients(isGranted);
                        break;
                    case "paiements":
                        ressource.setPaiements(isGranted);
                        break;
                    case "produits":
                        ressource.setProduits(isGranted);
                        break;
                    case "rapports_financiers":
                    case "rapports":
                        ressource.setRapportsFinanciers(isGranted);
                        break;
                    case "parametres_systemes":
                    case "parametres":
                        ressource.setParametresSystemes(isGranted);
                        break;
                    default:
                        throw new IllegalArgumentException("Ressource inconnue : " + ressourceNom);
                }

                ressourcesRepository.save(ressource);
            }
        }
        System.out.println("DEBUG - Matrice des droits synchronisée en BDD avec succès !");
    }
    */
    
    /*
    @Transactional
    public void mettreAJourPermissionParRole(String ressourceNom, String roleCibleStr, String action, boolean isGranted) {
        if (roleCibleStr == null) {
            throw new IllegalArgumentException("Le rôle cible ne peut pas être null.");
        }

        String cleanRole = roleCibleStr.toUpperCase().replace("ROLE_", "");
        Role roleCible = Role.valueOf(cleanRole);
        
        // 1. Récupérer tous les utilisateurs ayant ce rôle
        List<UtilisateurEntity> utilisateurs = utilisateurRepository.findByRole(roleCible);
        System.out.println("DEBUG - Utilisateurs trouvés pour le rôle " + roleCible + " : " + utilisateurs.size());

        for (UtilisateurEntity utilisateur : utilisateurs) {
            
            // 2. Mise à jour de l'ACTION dans la table utilisateurs
            if (action != null) {
                switch (action.toLowerCase().trim()) {
                    case "modifier":
                        utilisateur.setModifier(isGranted);
                        break;
                    case "lire":
                        utilisateur.setLire(isGranted);
                        break;
                    case "creer":
                        utilisateur.setCreer(isGranted);
                        break;
                    case "supprimer":
                        utilisateur.setSupprimer(isGranted);
                        break;
                    case "imprimer":
                        utilisateur.setImprimer(isGranted);
                        break;
                    case "sauvegarder":
                        utilisateur.setSauvegarder(isGranted);
                        break;
                    default:
                        System.out.println("⚠️ Action non reconnue : " + action);
                }
                utilisateurRepository.save(utilisateur);
                utilisateurRepository.flush(); // Force l'écriture immédiate en BDD
            }
            // 3. Mise à jour de la RESSOURCE dans la table ressources
            if (ressourceNom != null) {
                RessourcesEntity ressource = ressourcesRepository.findByUtilisateursId(utilisateur.getId().toString())
                    .orElseGet(() -> {
                        RessourcesEntity nouvelleRessource = new RessourcesEntity();
                        nouvelleRessource.setId(UUID.randomUUID());
                        nouvelleRessource.setUtilisateursId(utilisateur.getId().toString());
                        return nouvelleRessource;
                    });
                
                System.out.println("DEBUG AVANT - Valeur actuelle de clients pour la ressource : " + ressource.getClients());

                switch (ressourceNom.toLowerCase().trim()) {
                    case "clients":
                        ressource.setClients(isGranted);
                        break;
                    case "paiements":
                        ressource.setPaiements(isGranted);
                        break;
                    case "produits":
                        ressource.setProduits(isGranted);
                        break;
                    case "rapports_financiers":                   
                        ressource.setRapportsFinanciers(isGranted);
                        break;
                    case "parametres_systemes":
                        ressource.setParametresSystemes(isGranted);
                        break;
                    default:
                        System.out.println("⚠️ Ressource inconnue : " + ressourceNom);
                }

                ressourcesRepository.save(ressource);
                System.out.println("DEBUG APRÈS - Nouvelle valeur souhaitée pour clients : " + ressource.getClients());
                //ressourcesRepository.flush(); // Force l'écriture immédiate en BDD
            }
        }
        utilisateurRepository.flush();
        ressourcesRepository.flush();
        System.out.println("✅ Synchronisation BDD réussie pour l'action [" + action + "] et la ressource [" + ressourceNom + "]");
    }
    */
    
    
    //ok
    /*
    @Transactional
    public void mettreAJourPermissionParRole(String ressource, String roleCibleStr, String action, boolean isGranted) {
        // 1. Convertir l'action en minuscule pour correspondre aux noms de colonnes BDD (ex: 'CREER' -> 'creer')
        String colonne = action.toLowerCase();
        
        // 2. Liste blanche de sécurité pour éviter toute injection SQL sur le nom de colonne
        List<String> colonnesValides = List.of("creer", "modifier", "supprimer", "lire", "sauvegarder", "imprimer");
        if (!colonnesValides.contains(colonne)) {
            throw new IllegalArgumentException("Action non supportée : " + action);
        }

        // 3. Exécution de la mise à jour globale pour le rôle
        String sql = "UPDATE utilisateurs SET " + colonne + " = ? WHERE role = ?";
        int lignesMisesAJour = jdbcTemplate.update(sql, isGranted, roleCibleStr);

        System.out.println("✅ " + lignesMisesAJour + " utilisateur(s) mis à jour pour le rôle " + roleCibleStr + " (" + colonne + " = " + isGranted + ")");
        this.mettreAJourPermissionParRole2(ressource, roleCibleStr, action, isGranted);
    }
    */
    
    /*
    @Transactional
    public void mettreAJourPermissionParRole(String ressource, String roleCibleStr, String action, boolean isGranted) {
        String colonne = action.toLowerCase();
        
        List<String> colonnesValides = List.of("creer", "modifier", "supprimer", "lire", "sauvegarder", "imprimer");
        if (!colonnesValides.contains(colonne)) {
            throw new IllegalArgumentException("Action non supportée : " + action);
        }

        String sql = "UPDATE utilisateurs SET " + colonne + " = ? WHERE role = ?";
        
        // jdbcTemplate.update retourne le nombre de lignes modifiées en BDD
        int lignesModifiees = jdbcTemplate.update(sql, isGranted, roleCibleStr);

        System.out.println("🔍 DEBUG BDD : " + lignesModifiees + " ligne(s) mise(s) à jour pour le rôle '" + roleCibleStr + "' (colonne: " + colonne + ")");
    }
    */
    
    /*
    @Transactional
    public void mettreAJourPermissionParRole(String ressource, String roleCibleStr, String action, boolean isGranted) {
        // 1. Valider le nom de la ressource pour éviter les injections SQL (ex: 'clients')
        String nomRessource = ressource.toLowerCase();
        
        // 2. Requête pour mettre à jour la colonne correspondant à la ressource pour un rôle donné
        String sql = "UPDATE ressources SET " + nomRessource + " = ? WHERE role = ?";
        
        int lignesModifiees = jdbcTemplate.update(sql, isGranted, roleCibleStr);
        //System.println("🔍 DEBUG RESSOURCES : " + lignesModifiees + " ligne(s) mise(s) à jour pour la ressource '" + nomRessource + "' et le rôle '" + roleCibleStr + "'");
    }
    */
    
    /*
    @Transactional
    public void mettreAJourPermissionParRole(String ressource, String roleCibleStr, String action, boolean isGranted) {
        String colonneAction = action.toLowerCase(); // ex: 'creer'
        
        // On met à jour la ligne où le nom de la ressource correspond et pour le bon rôle
        String sql = "UPDATE ressources SET " + colonneAction + " = ? WHERE nom_ressource = ? AND role = ?";
        
        int lignesModifiees = jdbcTemplate.update(sql, isGranted, ressource, roleCibleStr);
        //System.println("🔍 DEBUG RESSOURCES : " + lignesModifiees + " ligne(s) mise(s) à jour pour la ressource '" + ressource + "'");
    }
    */
    
    /*
    @Transactional
    public void mettreAJourPermissionParRole2(String ressource, String roleCibleStr, String action, boolean isGranted) {
        // ressource contient par exemple "clients" ou "paiements" (qui est le nom de la colonne)
        // roleCibleStr contient le rôle (ex: "EMPLOYE")
        
        // Concaténation sécurisée du nom de la colonne ressource
        String sql = "UPDATE ressources SET " + ressource + " = ?";
        
        int lignesModifiees = jdbcTemplate.update(sql, isGranted);
        System.out.println("🔍 DEBUG RESSOURCES : " + lignesModifiees + " ligne(s) mise(s) à jour.");
    }
    */
    
    /*
    @Transactional
    public void mettreAJourPermissionParRole(String ressource, String roleCibleStr, boolean isGranted) {
        // 1. Liste blanche pour sécuriser le nom de la colonne (ressource) contre les injections SQL
        List<String> colonnesAutorisees = List.of(
            "clients", "paiements", "produits", 
            "rapports_financiers", "parametres_systemes", "comptes_clients"
        );
        
        if (!colonnesAutorisees.contains(ressource)) {
            throw new IllegalArgumentException("Ressource non autorisée : " + ressource);
        }

        // 2. Requête SQL avec la colonne "utilisateurs_id" dans le WHERE
        String sql = "UPDATE ressources SET " + ressource + " = ? WHERE utilisateurs_id = ?";
        
        int lignesModifiees = jdbcTemplate.update(sql, isGranted, roleCibleStr);
        System.out.println("🔍 DEBUG RESSOURCES : " + lignesModifiees + " ligne(s) mise(s) à jour pour [" + ressource + " / " + roleCibleStr + "]");
    }
    */
    
    /*
    @Transactional
    public void mettreAJourPermissionParRole(String nomChamp, String roleCibleStr, String action, boolean isGranted) {
        
        // 1. Définition des listes blanches pour sécuriser les noms de colonnes et cibler la bonne table
        List<String> actionsUtilisateurs = List.of("creer", "modifier", "supprimer", "lire", "sauvegarder", "imprimer");
        List<String> colonnesRessources = List.of("clients", "paiements", "produits", "rapports_financiers", "parametres_systemes");

        String champMinuscule = nomChamp.toLowerCase();
        int lignesModifiees = 0;

        // 2. Vérification et mise à jour de la table "utilisateurs" si c'est une action
        if (actionsUtilisateurs.contains(champMinuscule)) {
            // Adaptez "role" ou "id" selon le nom de la colonne qui identifie le rôle dans votre table utilisateurs
            String sql = "UPDATE utilisateurs SET " + champMinuscule + " = ? WHERE role = ?";
            lignesModifiees = jdbcTemplate.update(sql, isGranted, roleCibleStr);
            System.out.println("🔍 DEBUG UTILISATEURS : " + lignesModifiees + " ligne(s) mise(s) à jour pour l'action [" + champMinuscule + "]");
        } 
        // 3. Sinon, vérification et mise à jour de la table "ressources" si c'est une ressource
        else if (colonnesRessources.contains(nomChamp)) {
            String sql = "UPDATE ressources SET " + nomChamp + " = ? WHERE utilisateurs_id = ?";
            lignesModifiees = jdbcTemplate.update(sql, isGranted, roleCibleStr);
            System.out.println("🔍 DEBUG RESSOURCES : " + lignesModifiees + " ligne(s) mise(s) à jour pour la ressource [" + nomChamp + "]");
        } 
        else {
            throw new IllegalArgumentException("Champ non reconnu ou non autorisé : " + nomChamp);
        }
    }
    */
    
    public List<Map<String, Object>> obtenirToutesLesPermissions() {
        // Jointure pour récupérer en une seule fois les actions globales et les ressources par utilisateur/rôle
        String sql = "SELECT u.role, u.creer, u.modifier, u.supprimer, u.lire, u.sauvegarder, u.imprimer, " +
                     "r.clients, r.paiements, r.produits, r.rapports_financiers, r.parametres_systemes, r.comptes_clients " +
                     "FROM utilisateurs u " +
                     "LEFT JOIN ressources r ON u.role = r.utilisateurs_id";
        
        return jdbcTemplate.queryForList(sql);
    }
    
    @Transactional
    public void mettreAJourPermissionParRole(String userId, String ressource, String action, boolean isGranted) {
        
        // Listes blanches pour sécuriser les noms de colonnes
        List<String> actionsUtilisateurs = List.of("creer", "modifier", "supprimer", "lire", "sauvegarder", "imprimer");
        List<String> colonnesRessources = List.of("clients", "paiements", "produits", "rapports_financiers", "parametres_systemes", "comptes_clients");

        // 1. Si une action globale est fournie -> Table "utilisateurs"
        if (action != null && !action.isBlank() && actionsUtilisateurs.contains(action.toLowerCase())) {
            String sql = "UPDATE utilisateurs SET " + action.toLowerCase() + " = ? WHERE role = ?";
            int lignes = jdbcTemplate.update(sql, isGranted, userId);
            System.out.println("🔍 DEBUG UTILISATEURS : " + lignes + " ligne(s) mise(s) à jour pour l'action [" + action + "]");
        } 
        
        // 2. Si une ressource spécifique est fournie -> Table "ressources" avec UPSERT
        if (ressource != null && !ressource.isBlank() && colonnesRessources.contains(ressource.toLowerCase())) {
            String nomColonne = ressource.toLowerCase();
            
            // Requête PostgreSQL UPSERT : 
            // Si la ligne avec cet utilisateurs_id existe -> UPDATE
            // Sinon -> INSERT de la ligne avec l'utilisateurs_id et la valeur de la ressource
            String sql = "INSERT INTO ressources (utilisateurs_id, " + nomColonne + ") " +
                         "VALUES (?, ?) " +
                         "ON CONFLICT (utilisateurs_id) " +
                         "DO UPDATE SET " + nomColonne + " = EXCLUDED." + nomColonne;
            
            int lignes = jdbcTemplate.update(sql, userId, isGranted);
            System.out.println("🔍 DEBUG RESSOURCES : " + lignes + " ligne(s) traitée(s) (Insert/Update) pour la ressource [" + ressource + "]");
        } 
    }
  
}