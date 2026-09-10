package com.paybank.hexagonal.port;

import java.util.List;
import java.util.Optional;
import com.paybank.hexagonal.domaine.Role;
import com.paybank.hexagonal.domaine.Utilisateur;
import com.paybank.hexagonal.entity.UtilisateurEntity;

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