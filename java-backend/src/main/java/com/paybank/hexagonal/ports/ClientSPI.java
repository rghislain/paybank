package com.paybank.hexagonal.ports;

import com.paybank.hexagonal.domaine.Client;
import java.util.Optional;
import java.util.UUID;

public interface ClientSPI {
    void sauvegarder(Client client);
    Optional<Client> trouverParId(UUID id);
    void supprimer(UUID id);
}