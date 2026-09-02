package com.paybank.hexagonal.adaptateur;

import com.paybank.hexagonal.domaine.service.FactureService;
import com.paybank.hexagonal.entity.FactureEntity;
import com.paybank.hexagonal.port.FactureRepositorySPI;
import com.paybank.hexagonal.repository.SpringDataFactureRepository;

import org.springframework.stereotype.Repository;

@Repository
public class FactureJpaAdapteur implements FactureRepositorySPI {

    private final SpringDataFactureRepository springDataRepository;

    public FactureJpaAdapteur(SpringDataFactureRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

	@Override
	public FactureEntity sauvegarder(FactureEntity facture) {
		return springDataRepository.save(facture);
	}
}