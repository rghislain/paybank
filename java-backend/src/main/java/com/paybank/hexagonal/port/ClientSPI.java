package com.paybank.hexagonal.port;

import com.paybank.hexagonal.domaine.Client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientSPI {
    void sauvegarder(Client client);
    Optional<Client> trouverParId(UUID id);
    void supprimer(UUID id);
    List<Client> listerTousLesClients();
}