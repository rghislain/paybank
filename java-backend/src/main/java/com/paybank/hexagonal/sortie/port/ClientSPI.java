package com.paybank.hexagonal.sortie.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.paybank.hexagonal.domaine.model.Client;

public interface ClientSPI {
    void sauvegarder(Client client);
    Optional<Client> trouverParId(UUID id);
    void supprimer(UUID id);
    List<Client> listerTousLesClients();
}