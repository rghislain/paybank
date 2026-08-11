package com.paybank.hexagonal.repository;

import java.util.Optional;

import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.entity.UtilisateurEntity;

@Repository
public interface UtilisateurRepository extends JpaRepository<UtilisateurEntity, String> {
	Utilisateur save(Utilisateur utilisateur);
	Optional<UtilisateurEntity> findByEmail(String email);
	Streamable<Order> findByNom(String nom);
}