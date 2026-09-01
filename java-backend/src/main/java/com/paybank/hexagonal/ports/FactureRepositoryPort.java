package com.paybank.hexagonal.ports;

import com.paybank.hexagonal.domaine.ServiceFacture;
import com.paybank.hexagonal.entity.FactureEntity;

public interface FactureRepositoryPort {
    FactureEntity sauvegarder(FactureEntity facture);
}