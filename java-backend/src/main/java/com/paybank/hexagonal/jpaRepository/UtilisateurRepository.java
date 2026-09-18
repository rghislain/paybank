package com.paybank.hexagonal.jpaRepository;

import com.paybank.hexagonal.domaine.model.Role;
import com.paybank.hexagonal.entite.UtilisateurEntity;

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
    //Utiliser l'entité JPA et non l'objet du domaine
    UtilisateurEntity save(UtilisateurEntity utilisateur);

    Optional<UtilisateurEntity> findByEmail(String email);
    
    List<UtilisateurEntity> findByRole(Role role);
    
    Optional<UtilisateurEntity> findByNom(String nom);

    @Modifying
    @Transactional
    @Query("UPDATE UtilisateurEntity u SET u.password = :nouveauMdp WHERE u.role = :role")
    void mettreAJourMotDePasseParRole(@Param("role") Role role, @Param("nouveauMdp") String nouveauMdp);
}