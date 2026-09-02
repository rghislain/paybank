package com.paybank.hexagonal.domaine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.paybank.hexagonal.domaine.Role;
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
    
    private final JdbcTemplate jdbcTemplate;
    
    public GestionDroitsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void mettreAJourDroitsParRole(Role role, String nomRessource, boolean valeur) {
        List<UtilisateurEntity> utilisateurs = utilisateurRepository.findByRole(role);

        for (UtilisateurEntity utilisateur : utilisateurs) {
            // Utilisation du getter de l'entité UtilisateurEntity       	
            //RessourcesEntity ressource = utilisateur.getRessource();
        	
        	// S'assure que la liste de ressources existe
            if (utilisateur.getRessources() == null) {
                utilisateur.setRessources(new ArrayList<>());
            }
            
            RessourcesEntity ressource = null;
            
            /*
            if (ressource == null) {
                ressource = new RessourcesEntity();
                ressource.setId(UUID.randomUUID());
                ressource.setUtilisateur(utilisateur);
            }
            */
            
            if (utilisateur.getRessources().isEmpty()) {
                ressource = new RessourcesEntity();
                ressource.setId(UUID.randomUUID());
                ressource.setUtilisateur(utilisateur);
                utilisateur.getRessources().add(ressource);
            } else {
                // On prend la première ressource existante de la liste
                ressource = utilisateur.getRessources().get(0);
            }
           
            switch (nomRessource.toLowerCase().trim()) {
                case "clients":
                    ressource.setClients(valeur);
                    break;
                case "paiements":
                    ressource.setPaiements(valeur);
                    break;
                case "utilisateurs":
                    ressource.setUtilisateurs(valeur);
                    break;
                case "produits":
                    ressource.setProduits(valeur);
                    break;
                case "rapports_financiers":
                case "rapports":
                    ressource.setRapportsFinanciers(valeur);
                    break;
                case "parametres_systemes":
                case "parametres":
                    ressource.setParametresSystemes(valeur);
                    break;
                default:
                    throw new IllegalArgumentException("Ressource inconnue : " + nomRessource);
            }
            
            
            ressourcesRepository.save(ressource);
        }
    }
    
    public List<Map<String, Object>> obtenirToutesLesPermissions() {
        String sql = "SELECT u.role, u.creer, u.modifier, u.supprimer, u.lire, u.sauvegarder, u.imprimer, " +
                     "r.clients, r.paiements, r.produits, r.rapports_financiers, r.parametres_systemes " +
                     "FROM utilisateurs u " +
                     "LEFT JOIN ressources r ON u.id = r.utilisateurs_id";
        
        return jdbcTemplate.queryForList(sql);
    }
    
    @Transactional
    public void mettreAJourPermissionParRole(String userId, String ressource, String action, boolean isGranted) {
        List<String> actionsUtilisateurs = List.of("creer", "modifier", "supprimer", "lire", "sauvegarder", "imprimer");
        List<String> colonnesRessources = List.of("clients", "paiements", "produits", "rapports_financiers", "parametres_systemes");

        if (action != null && !action.isBlank() && actionsUtilisateurs.contains(action.toLowerCase().trim())) {
            String sql = "UPDATE utilisateurs SET " + action.toLowerCase().trim() + " = ? WHERE role = ?";
            jdbcTemplate.update(sql, isGranted, userId);
        } 
        
        if (ressource != null && !ressource.isBlank() && colonnesRessources.contains(ressource.toLowerCase().trim())) {
            String nomColonne = ressource.toLowerCase().trim();
            String sql = "INSERT INTO ressources (id, utilisateurs_id, " + nomColonne + ") " +
                         "VALUES (?, (SELECT id FROM utilisateurs WHERE role = ? LIMIT 1), ?) " +
                         "ON CONFLICT (utilisateurs_id) " +
                         "DO UPDATE SET " + nomColonne + " = EXCLUDED." + nomColonne;
            
            jdbcTemplate.update(sql, UUID.randomUUID(), userId, isGranted);
        }
    }
}