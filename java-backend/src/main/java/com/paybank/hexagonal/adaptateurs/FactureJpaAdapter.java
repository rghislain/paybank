package com.paybank.hexagonal.adaptateurs;

import com.paybank.hexagonal.domaine.ServiceFacture;
import com.paybank.hexagonal.entity.FactureEntity;
import com.paybank.hexagonal.ports.FactureRepositoryPort;
import com.paybank.hexagonal.repository.SpringDataFactureRepository;

import org.springframework.stereotype.Repository;

@Repository
public class FactureJpaAdapter implements FactureRepositoryPort {

    private final SpringDataFactureRepository springDataRepository;

    public FactureJpaAdapter(SpringDataFactureRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

	@Override
	public FactureEntity sauvegarder(FactureEntity facture) {
		return springDataRepository.save(facture);
	}
}