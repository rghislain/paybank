package com.paybank.hexagonal.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.paybank.hexagonal.domaine.ServiceAnnulation;
import com.paybank.hexagonal.domaine.ServiceModifierPaiement;
import com.paybank.hexagonal.domaine.ServicePaiementImplementation;
import com.paybank.hexagonal.domaine.TransactionPaiement;
import com.paybank.hexagonal.ports.AnnulerPaiementUseCase;
import com.paybank.hexagonal.ports.ExecutionPaiementUseCase;
import com.paybank.hexagonal.ports.ModifierPaiementUseCase;
import com.paybank.hexagonal.ports.PasserelleBancaireSPI;
import com.paybank.hexagonal.ports.PersistancePaiementSPI;
import com.paybank.hexagonal.ports.TransactionRepositorySPI;

@Configuration
public class PaiementConfiguration {
	@Bean
    public ExecutionPaiementUseCase executionPaiementUseCase(PersistancePaiementSPI persistancePaiementSPI, PasserelleBancaireSPI passerelleBancaireSPI, TransactionRepositorySPI transactionRepositorySPI) {
        // Instanciation du code métier pur avec ses deux adaptateurs
        return new ServicePaiementImplementation(persistancePaiementSPI, passerelleBancaireSPI, transactionRepositorySPI);
    }
	
	@Bean
    public AnnulerPaiementUseCase annulationService(PasserelleBancaireSPI passerelleBancaireSPI, PersistancePaiementSPI persistancePaiementSPI) {
        return new ServiceAnnulation(passerelleBancaireSPI, persistancePaiementSPI);
    }
	
	@Bean
	public ModifierPaiementUseCase modifierPaiement(PersistancePaiementSPI persistancePaiementSPI) {
		return new ServiceModifierPaiement(persistancePaiementSPI);
	}
}