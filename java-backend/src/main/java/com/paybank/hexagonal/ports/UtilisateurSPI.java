package com.paybank.hexagonal.ports;

import java.util.Optional;
import com.paybank.hexagonal.domaine.Utilisateur;

public interface UtilisateurSPI {
    Utilisateur save(Utilisateur utilisateur);
    Optional<Utilisateur> findById(String id);
    void delete(String id);
    Optional<Utilisateur> findByEmail(String email);
}