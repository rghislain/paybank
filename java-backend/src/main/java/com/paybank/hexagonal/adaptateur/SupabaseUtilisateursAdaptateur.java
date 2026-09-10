package com.paybank.hexagonal.adaptateur;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.jdbc.core.JdbcTemplate;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.repository.UtilisateurRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.entity.RolePermissionsEntity;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.port.UtilisateurSPI;
import com.paybank.hexagonal.domaine.service.GestionDroitsService;

@Component
public class SupabaseUtilisateursAdaptateur implements UtilisateurSPI {

    private final UtilisateurRepository utilisateurRepository;
	
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private GestionDroitsService gestionDroitsService;
    
    //Injection propre du repository Spring Data (par le constructeur)
    public SupabaseUtilisateursAdaptateur(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }
 
    @Override
    public Utilisateur save(Utilisateur utilisateurDomaine) {
        //Utilisation du convertisseur centralisé pour ne rater aucun champ (id, nom, email, rôle, actif)
        UtilisateurEntity entityToSave = UtilisateurEntity.fromDomain(utilisateurDomaine);

        //Sauvegarde dans le dépôt Spring Data JPA
        UtilisateurEntity entitySauvegardee = utilisateurRepository.save(entityToSave);

        //Re-conversion vers le domaine
        return entitySauvegardee.toDomain();
    }
    
    @Override
    public Utilisateur sauvegarderUtilisateurAvecDroits(Utilisateur utilisateurDomaine) {
        //1. Récupération de l'entité existante en BDD (évite la création de doublons)
        UtilisateurEntity entity = utilisateurRepository.findById(utilisateurDomaine.getId())
                .orElseGet(UtilisateurEntity::new);

        //2. Mappage des informations de base
        entity.setId(utilisateurDomaine.getId());
        entity.setNom(utilisateurDomaine.getNom());
        entity.setEmail(utilisateurDomaine.getEmail());
        entity.setRole(utilisateurDomaine.getRole());
        entity.setActif(utilisateurDomaine.isActive());
 
        //3. Sauvegarde immédiate en BDD (UPDATE propre dans 'utilisateurs' et 'role_permissions')
        UtilisateurEntity savedEntity = utilisateurRepository.save(entity);

        return savedEntity.toDomain();
    }
          
    public void mettreAJourMatriceDroits(String utilisateurId, boolean creer, boolean lire, boolean modifier, boolean supprimer, boolean imprimer, boolean sauvegarder) {
        String sql = "UPDATE utilisateurs SET creer = ?, lire = ?, modifier = ?, supprimer = ? WHERE id = ?";
        
        jdbcTemplate.update(sql, creer, lire, modifier, supprimer, imprimer, sauvegarder, utilisateurId);
    }
      
    @Override
    public void basculerDroitPourRole(Role role, String nomDroit, boolean valeur) {
        //Délègue à GestionDroitsService (role_permissions), seule source de vérité
        //pour les droits. Comme cette méthode ne reçoit pas de ressource, elle
        // applique la valeur sur TOUTES les ressources pour l'action demandée.
        gestionDroitsService.mettreAJourActionPourToutesLesRessources(role, nomDroit, valeur);
    }
    

    @Override
    public Optional<Utilisateur> findById(String id) {
        //On va chercher l'entité, et si elle existe, on exécute le mapping .toDomain()
        return utilisateurRepository.findById(id)
                .map(UtilisateurEntity::toDomain);
    }

    @Override
    public void delete(String id) {
        //Soft delete conforme aux règles du Domaine
        utilisateurRepository.findById(id).ifPresent(entity -> {
            Utilisateur userDomain = entity.toDomain();
            userDomain.deactivate(); //Logique métier de désactivation            
            //On ré-enregistre l'état désactivé
            utilisateurRepository.save(UtilisateurEntity.fromDomain(userDomain));
        });
    }

	@Override
	public Optional<Utilisateur> findByEmail(String email) {
		 return utilisateurRepository.findByEmail(email)
	                .map(UtilisateurEntity::toDomain);
	}

	@Override
	public Optional<Utilisateur> findByNom(String nom) {
		UtilisateurEntity entity = (UtilisateurEntity) utilisateurRepository.findByNom(nom).get();
	    return Optional.ofNullable(entity).map(UtilisateurEntity::toDomain);
	}

	@Override
	public List<Utilisateur> listerTousLesSalaries() {
		//1. On récupère toutes les entités depuis Spring Data JPA
        List<UtilisateurEntity> entities = utilisateurRepository.findAll();
        System.out.println("nombre de salariés trouvés en BDD : " + entities.size());
        //2. On convertit chaque entité en objet du domaine via .toDomain() et on retourne la liste
        return entities.stream()
                .map(UtilisateurEntity::toDomain)
                .toList();
	}
	
}