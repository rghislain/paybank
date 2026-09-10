package com.paybank.hexagonal.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.paybank.hexagonal.domaine.TransactionPaiement;
import com.paybank.hexagonal.domaine.service.AnnulationService;
import com.paybank.hexagonal.domaine.service.ModifierPaiementService;
import com.paybank.hexagonal.domaine.service.PaiementImplementationService;
import com.paybank.hexagonal.port.AnnulerPaiementSPI;
import com.paybank.hexagonal.port.ExecutionPaiementSPI;
import com.paybank.hexagonal.port.ModifierPaiementSPI;
import com.paybank.hexagonal.port.PasserelleBancaireSPI;
import com.paybank.hexagonal.port.PersistancePaiementSPI;
import com.paybank.hexagonal.port.TransactionRepositorySPI;

@Configuration
public class PaiementConfiguration {
	@Bean
    public ExecutionPaiementSPI executionPaiementUseCase(PersistancePaiementSPI persistancePaiementSPI, PasserelleBancaireSPI passerelleBancaireSPI, TransactionRepositorySPI transactionRepositorySPI) {
        //Instanciation du code métier pur avec ses deux adaptateurs
        return new PaiementImplementationService(persistancePaiementSPI, passerelleBancaireSPI, transactionRepositorySPI);
    }
	
	@Bean
    public AnnulerPaiementSPI annulationService(PasserelleBancaireSPI passerelleBancaireSPI, PersistancePaiementSPI persistancePaiementSPI) {
        return new AnnulationService(passerelleBancaireSPI, persistancePaiementSPI);
    }
	
	@Bean
	public ModifierPaiementSPI modifierPaiement(PersistancePaiementSPI persistancePaiementSPI) {
		return new ModifierPaiementService(persistancePaiementSPI);
	}
}