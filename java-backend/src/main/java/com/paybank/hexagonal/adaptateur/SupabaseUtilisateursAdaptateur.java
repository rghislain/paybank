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

@Component
public class SupabaseUtilisateursAdaptateur implements UtilisateurSPI {

    private final UtilisateurRepository utilisateurRepository;
	
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // Injection propre du repository Spring Data (par le constructeur)
    public SupabaseUtilisateursAdaptateur(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /*
    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        // 1. Conversion du Domaine pur vers l'Entité de Base de données (Infrastructure)
        UtilisateurEntity entityToSave = UtilisateurEntity.fromDomain(utilisateur);
        
        // 2. Sauvegarde réelle en BDD via Spring Data
        UtilisateurEntity savedEntity = utilisateurRepository.save(entityToSave);
        
        // 3. Re-conversion vers le Domaine pour ressortir proprement de l'adaptateur
        return savedEntity.toDomain();
    }
    */
    
    /*
    @Override
    public Utilisateur save(Utilisateur utilisateurDomaine) {
        // 1. On cherche si l'entité existe déjà pour la mettre à jour, sinon on crée une nouvelle instance
        UtilisateurEntity entity = utilisateurRepository.findById(utilisateurDomaine.getId())
                .orElse(new UtilisateurEntity());

        // 2. MAPPING DES CHAMPS (Vérifiez bien que la ligne 'role' est présente !)
        entity.setId(utilisateurDomaine.getId());
        entity.setEmail(utilisateurDomaine.getEmail());
        entity.setNom(utilisateurDomaine.getNom());
        entity.setRole(utilisateurDomaine.getRole()); // 👈 TRÈS IMPORTANT : Ne pas l'oublier !

        // 3. Sauvegarde dans le dépôt Spring Data JPA
        UtilisateurEntity entitySauvegardee = utilisateurRepository.save(entity);

        // 4. On retransforme l'entité sauvegardée en objet du domaine pour la renvoyer au service
        return new Utilisateur(
            entitySauvegardee.getId(),
            entitySauvegardee.getEmail(),
            entitySauvegardee.getNom(),
            entitySauvegardee.getRole()
        );
    }
    */
    
    @Override
    public Utilisateur save(Utilisateur utilisateurDomaine) {
        // Utilisation de votre convertisseur centralisé pour ne rater aucun champ (id, nom, email, rôle, actif)
        UtilisateurEntity entityToSave = UtilisateurEntity.fromDomain(utilisateurDomaine);

        // Sauvegarde dans le dépôt Spring Data JPA
        UtilisateurEntity entitySauvegardee = utilisateurRepository.save(entityToSave);

        // Re-conversion vers le domaine
        return entitySauvegardee.toDomain();
    }
    
    @Override
    public Utilisateur sauvegarderUtilisateurAvecDroits(Utilisateur utilisateurDomaine) {
        // 1. Récupération de l'entité existante en BDD (évite la création de doublons)
        UtilisateurEntity entity = utilisateurRepository.findById(utilisateurDomaine.getId())
                .orElseGet(UtilisateurEntity::new);

        // 2. Mappage des informations de base
        entity.setId(utilisateurDomaine.getId());
        entity.setNom(utilisateurDomaine.getNom());
        entity.setEmail(utilisateurDomaine.getEmail());
        entity.setRole(utilisateurDomaine.getRole());
        entity.setActif(utilisateurDomaine.isActive());

        // 3. Gestion de la matrice des droits (Création si absente, mise à jour sinon)
        /*
        if (entity.getRolePermissions() == null) {
            entity.setRolePermissions(new RolePermissionsEntity());
        }

        // Passage explicite des valeurs cochées/décochées
        entity.getRolePermissions().setLire(utilisateurDomaine.isLire());
        entity.getRolePermissions().setCreer(utilisateurDomaine.isCreer());
        entity.getRolePermissions().setModifier(utilisateurDomaine.isModifier());
        entity.getRolePermissions().setSupprimer(utilisateurDomaine.isSupprimer());
        entity.getRolePermissions().setSauvegarder(utilisateurDomaine.isSauvegarder());
        entity.getRolePermissions().setImprimer(utilisateurDomaine.isImprimer());
		*/

        // 4. Sauvegarde immédiate en BDD (UPDATE propre dans 'utilisateurs' et 'role_permissions')
        UtilisateurEntity savedEntity = utilisateurRepository.save(entity);

        return savedEntity.toDomain();
    }
    
    /*
    public void basculerDroitPourRole(Role role, String nomDroit, boolean valeur) {
        switch (nomDroit.toLowerCase()) {
            case "lire":
                utilisateurRepository.mettreAJourDroitLireParRole(role, valeur);
                break;
            case "creer":
                utilisateurRepository.mettreAJourDroitCreerParRole(role, valeur);
                break;
            case "modifier":
                utilisateurRepository.mettreAJourDroitModifierParRole(role, valeur);
                break;
            case "supprimer":
                utilisateurRepository.mettreAJourDroitSupprimerParRole(role, valeur);
                break;
            case "sauvegarder":
                utilisateurRepository.mettreAJourDroitSauvegarderParRole(role, valeur);
                break;
            case "imprimer":
                utilisateurRepository.mettreAJourDroitImprimerParRole(role, valeur);
                break;
            default:
                throw new IllegalArgumentException("Droit inconnu : " + nomDroit);
        }
    }
    */
    
    /*
    @Override
    public void basculerDroitPourRole(Role role, String nomDroit, boolean valeur) {
        // 1. On récupère tous les utilisateurs qui ont ce rôle
        List<UtilisateurEntity> utilisateurs = utilisateurRepository.findByRole(role);

        for (utilisateurs : UtilisateurEntity user) {
            // 2. Si la table liée 'role_permissions' est vide (null), on l'initialise
            if (user.getRolePermissions() == null) {
                user.setRolePermissions(new RolePermissionsEntity());
            }

            // 3. On met à jour le droit correspondant
            switch (nomDroit.toLowerCase()) {
                case "lire": user.getRolePermissions().setLire(valeur); break;
                case "creer": user.getRolePermissions().setCreer(valeur); break;
                case "modifier": user.getRolePermissions().setModifier(valeur); break;
                case "supprimer": user.getRolePermissions().setSupprimer(valeur); break;
                case "sauvegarder": user.getRolePermissions().setSauvegarder(valeur); break;
                case "imprimer": user.getRolePermissions().setImprimer(valeur); break;
            }
        }

        // 4. On sauvegarde tout proprement (JPA gère la clé étrangère et l'INSERT/UPDATE dans role_permissions)
        utilisateurRepository.saveAll(utilisateurs);
    }
    */
    
    /*
    @Override
    public void basculerDroitPourRole(Role role, String nomDroit, boolean valeur) {
        // 1. On récupère tous les utilisateurs qui ont ce rôle
        List<UtilisateurEntity> listeUtilisateurs = utilisateurRepository.findByRole(role);

        // 2. Correction de la boucle for (Type element : collection)
        for (UtilisateurEntity utilisateur : listeUtilisateurs) {
            
            // Si la table liée 'role_permissions' n'existe pas encore, on l'initialise
            if (utilisateur.getRolePermissions() == null) {
                utilisateur.setRolePermissions(new RolePermissionsEntity());
            }

            // 3. On met à jour le droit correspondant selon la case cochée/décochée
            switch (nomDroit.toLowerCase()) {
                case "lire": 
                    utilisateur.getRolePermissions().setLire(valeur); 
                    break;
                case "creer": 
                    utilisateur.getRolePermissions().setCreer(valeur); 
                    break;
                case "modifier": 
                    utilisateur.getRolePermissions().setModifier(valeur); 
                    break;
                case "supprimer": 
                    utilisateur.getRolePermissions().setSupprimer(valeur); 
                    break;
                case "sauvegarder": 
                    utilisateur.getRolePermissions().setSauvegarder(valeur); 
                    break;
                case "imprimer": 
                    utilisateur.getRolePermissions().setImprimer(valeur); 
                    break;
            }
        }

        // 4. On sauvegarde tout en lot : Hibernate gère la liaison par clé étrangère et la BDD se met à jour
        utilisateurRepository.saveAll(listeUtilisateurs);
    }
    */
    
    public void mettreAJourMatriceDroits(String utilisateurId, boolean creer, boolean lire, boolean modifier, boolean supprimer, boolean imprimer, boolean sauvegarder) {
        String sql = "UPDATE utilisateurs SET creer = ?, lire = ?, modifier = ?, supprimer = ? WHERE id = ?";
        
        jdbcTemplate.update(sql, creer, lire, modifier, supprimer, imprimer, sauvegarder, utilisateurId);
    }
    
    @Override
    public void basculerDroitPourRole(Role role, String nomDroit, boolean valeur) {
        switch (nomDroit.toLowerCase()) {
            case "lire":
                utilisateurRepository.mettreAJourDroitLireParRole(role, valeur);
                break;
            case "creer":
                utilisateurRepository.mettreAJourDroitCreerParRole(role, valeur);
                break;
            case "modifier":
                utilisateurRepository.mettreAJourDroitModifierParRole(role, valeur);
                break;
            case "supprimer":
                utilisateurRepository.mettreAJourDroitSupprimerParRole(role, valeur);
                break;
            case "sauvegarder":
                utilisateurRepository.mettreAJourDroitSauvegarderParRole(role, valeur);
                break;
            case "imprimer":
                utilisateurRepository.mettreAJourDroitImprimerParRole(role, valeur);
                break;
            default:
                throw new IllegalArgumentException("Droit inconnu : " + nomDroit);
        }
    }
    

    @Override
    public Optional<Utilisateur> findById(String id) {
        // On va chercher l'entité, et si elle existe, on exécute le mapping .toDomain()
        return utilisateurRepository.findById(id)
                .map(UtilisateurEntity::toDomain);
    }

    @Override
    public void delete(String id) {
        // Soft delete conforme aux règles du Domaine
        utilisateurRepository.findById(id).ifPresent(entity -> {
            Utilisateur userDomain = entity.toDomain();
            userDomain.deactivate(); // Logique métier de désactivation
            
            // On ré-enregistre l'état désactivé
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
	    //return utilisateurRepository.findByNom(nom)
	            //.map(entity -> entity.toDomain());
	}

	@Override
	public List<Utilisateur> listerTousLesSalaries() {
		// 1. On récupère toutes les entités depuis Spring Data JPA
        List<UtilisateurEntity> entities = utilisateurRepository.findAll();
        System.out.println("nombre de salariés trouvés en BDD : " + entities.size());
        // 2. On convertit chaque entité en objet du domaine via .toDomain() et on retourne la liste
        return entities.stream()
                .map(UtilisateurEntity::toDomain)
                .toList();
	}
	
}