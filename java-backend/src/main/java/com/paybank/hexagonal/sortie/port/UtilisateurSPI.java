package com.paybank.hexagonal.sortie.port;

import java.util.List;
import java.util.Optional;

import com.paybank.hexagonal.domaine.model.Role;
import com.paybank.hexagonal.domaine.model.Utilisateur;
import com.paybank.hexagonal.entite.UtilisateurEntity;

public interface UtilisateurSPI {
    Utilisateur save(Utilisateur utilisateur);
    Optional<Utilisateur> findById(String id);
    void delete(String id);
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByNom(String nom);
    List<Utilisateur> listerTousLesSalaries();
	Utilisateur sauvegarderUtilisateurAvecDroits(Utilisateur utilisateurDomaine);
	void basculerDroitPourRole(Role role, String nomDroit, boolean valeur);
}