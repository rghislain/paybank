package com.paybank.hexagonal.sortie.port;

import com.paybank.hexagonal.domaine.service.FactureService;
import com.paybank.hexagonal.entite.FactureEntity;

public interface FactureSPI {
    FactureEntity sauvegarder(FactureEntity facture);
}