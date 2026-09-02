package com.paybank.hexagonal.port;

import com.paybank.hexagonal.domaine.service.FactureService;
import com.paybank.hexagonal.entity.FactureEntity;

public interface FactureRepositorySPI {
    FactureEntity sauvegarder(FactureEntity facture);
}