package com.paybank.hexagonal.adaptateurs;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.ports.UtilisateurSPI;
import com.paybank.hexagonal.repository.UtilisateurRepository;

import java.util.Optional;
import org.springframework.stereotype.Component;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.ports.UtilisateurSPI;

@Component
public class SupabaseUtilisateursAdaptateur implements UtilisateurSPI {

    private final UtilisateurRepository utilisateurRepository;
	
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
		UtilisateurEntity entity = (UtilisateurEntity) utilisateurRepository.findByNom(nom);
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