package com.paybank.hexagonal.sortie.port.adaptateur;

import com.paybank.hexagonal.domaine.service.FactureService;
import com.paybank.hexagonal.entite.FactureEntity;
import com.paybank.hexagonal.jpaRepository.SpringDataFactureRepository;
import com.paybank.hexagonal.sortie.port.FactureSPI;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public class PersistanceFactureAdapteur implements FactureSPI {

    private final SpringDataFactureRepository springDataRepository;

    public PersistanceFactureAdapteur(SpringDataFactureRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

	@Override
	public FactureEntity sauvegarder(FactureEntity facture) {
		return springDataRepository.save(facture);
	}
}