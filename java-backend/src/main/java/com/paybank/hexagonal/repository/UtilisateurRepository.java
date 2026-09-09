package com.paybank.hexagonal.repository;

import com.paybank.hexagonal.entity.UtilisateurEntity;
import com.paybank.hexagonal.domaine.Role;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UtilisateurRepository extends JpaRepository<UtilisateurEntity, String> {

    // Utiliser l'entité JPA et non l'objet du domaine
    UtilisateurEntity save(UtilisateurEntity utilisateur);

    Optional<UtilisateurEntity> findByEmail(String email);

    // Corrigé pour renvoyer une liste d'entités (ajustez selon votre propriété nom)
    //List<UtilisateurEntity> findByNom(String nom);
    
    List<UtilisateurEntity> findByRole(Role role);
    
    //Optional<UtilisateurEntity> findByEmail2(String email);
    
    Optional<UtilisateurEntity> findByNom(String nom);

    @Modifying
    @Transactional
    @Query("UPDATE UtilisateurEntity u SET u.password = :nouveauMdp WHERE u.role = :role")
    void mettreAJourMotDePasseParRole(@Param("role") Role role, @Param("nouveauMdp") String nouveauMdp);

    /*
    @Modifying
    @Transactional
    @Query("UPDATE UtilisateurEntity u SET u.lire = :valeur WHERE u.role = :role")
    void mettreAJourDroitLireParRole(@Param("role") Role role, @Param("valeur") boolean valeur);

    @Modifying
    @Transactional
    @Query("UPDATE UtilisateurEntity u SET u.creer = :valeur WHERE u.role = :role")
    void mettreAJourDroitCreerParRole(@Param("role") Role role, @Param("valeur") boolean valeur);

    @Modifying
    @Transactional
    @Query("UPDATE UtilisateurEntity u SET u.modifier = :valeur WHERE u.role = :role")
    void mettreAJourDroitModifierParRole(@Param("role") Role role, @Param("valeur") boolean valeur);

    @Modifying
    @Transactional
    @Query("UPDATE UtilisateurEntity u SET u.supprimer = :valeur WHERE u.role = :role")
    void mettreAJourDroitSupprimerParRole(@Param("role") Role role, @Param("valeur") boolean valeur);

    @Modifying
    @Transactional
    @Query("UPDATE UtilisateurEntity u SET u.sauvegarder = :valeur WHERE u.role = :role")
    void mettreAJourDroitSauvegarderParRole(@Param("role") Role role, @Param("valeur") boolean valeur);

    @Modifying
    @Transactional
    @Query("UPDATE UtilisateurEntity u SET u.imprimer = :valeur WHERE u.role = :role")
    void mettreAJourDroitImprimerParRole(@Param("role") Role role, @Param("valeur") boolean valeur);
 	*/
}