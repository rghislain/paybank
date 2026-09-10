package com.paybank.hexagonal.tests;

import com.paybank.hexagonal.adaptateur.StripeMokbankAdaptateur;
import com.paybank.hexagonal.domaine.MontantCentimes;
import com.paybank.hexagonal.domaine.TransactionPaiement;
import com.paybank.hexagonal.domaine.service.AnnulationService;
import com.paybank.hexagonal.main.PaiementApplication;
import com.paybank.hexagonal.port.AnnulerPaiementSPI;
import com.paybank.hexagonal.port.ExecutionPaiementSPI;
import com.paybank.hexagonal.port.ModifierPaiementSPI;
import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

@SpringBootTest(classes = PaiementApplication.class)
@TestPropertySource(properties = {	   
		//On redirige l'URL vers les vrais serveurs de test de Stripe !
	    "stripe.api.url=https://api.stripe.com/v1/charges"
})
class PaiementIntegrationTest {
    @Autowired
    private ExecutionPaiementSPI executionPaiementUseCase;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private AnnulerPaiementSPI annulerPaiementUseCase; //Spring va injecter l'implémentation automatiquement
    
    @SpyBean
    private StripeMokbankAdaptateur stripeMokbankAdaptateur;
    
    @Autowired
    private ModifierPaiementSPI modifierPaiementUseCase;
       
    @BeforeEach
    void setUp() {
        //On force le SDK Stripe à utiliser HTTP et le port de Mokbank
        Stripe.overrideApiBase("http://java-backend-api:8080"); 
    }
    
    void creerClient(UUID clientID) {
    	String sql="INSERT INTO clients (id, nom, email) VALUES (?::uuid, ?, ?) ON CONFLICT (id) DO NOTHING";
    	 jdbcTemplate.update(sql, clientID.toString(), "Client Test", "test_" + clientID + "@paybank.com");
    }

    @Test
    void simulerUnPaiementReussi() {
        //1. PRÉPARATION DES DONNÉES DE SIMULATION
        UUID compteClientIdFictif = UUID.randomUUID();
        this.creerClient(compteClientIdFictif);
        int montantEnCentimes = 2500; //Simule un panier de 25,00 €
        String cleIdempotenceUnique = UUID.randomUUID().toString(); //Clé d'idempotence unique
        //Jeton Stripe de test officiel pour simuler une carte valide
        //(Stripe intercepte ce token et renverra toujours 'succeeded')
        String tokenCarteStripeTest = "tok_visa"; 
        
        //2. EXÉCUTION DU CAS D'USAGE DANS L'HEXAGONE
        System.out.println("--- DÉBUT DE LA SIMULATION DU PAIEMENT ---");
        TransactionPaiement transactionResultat = executionPaiementUseCase.traiterPaiement(
                compteClientIdFictif,
                montantEnCentimes,
                cleIdempotenceUnique,
                tokenCarteStripeTest
        );
        System.out.println("--- FIN DE LA SIMULATION DU PAIEMENT ---");

        //3. VÉRIFICATIONS (ASSERTIONS)
        assertNotNull(transactionResultat);
        assertNotNull(transactionResultat.getId(), "L'UUID de la transaction doit être généré.");
        assertEquals(TransactionPaiement.StatutTransaction.SUCCESS, transactionResultat.getStatut(), 
                "Le paiement Stripe avec 'tok_visa' aurait dû passer au statut SUCCESS.");
        
        System.out.println("Paiement simulé avec succès : SUCCES EN BDD !");
        System.out.println("ID Transaction généré : " + transactionResultat.getId());
        System.out.println("Statut final en Base Supabase : " + transactionResultat.getStatut());
    }
    
    @Test
    void simulerUnPaiementEchouePourProvisionInsufisante() {
        //1. PRÉPARATION DES DONNÉES DE SIMULATION
        UUID compteClientIdFictif = UUID.randomUUID();
        this.creerClient(compteClientIdFictif);
        int montantEnCentimes = 2500; //Simule un panier de 25,00 €
        String cleIdempotenceUnique = UUID.randomUUID().toString(); //Clé d'idempotence unique      
        //Jeton Stripe de test officiel pour simuler une carte valide
        //(Stripe intercepte ce token et renverra toujours 'succeeded')
        String tokenCarteStripeTest = "tok_chargeDeclinedInsufficientFunds"; 

        //2. EXÉCUTION DU CAS D'USAGE DANS L'HEXAGONE
        System.out.println("--- DÉBUT DE LA SIMULATION DU PAIEMENT ---");
        TransactionPaiement transactionResultat = 
	        		executionPaiementUseCase.traiterPaiement(
		                compteClientIdFictif,
		                montantEnCentimes,
		                cleIdempotenceUnique,
		                tokenCarteStripeTest
	        		);      		
        System.out.println("--- FIN DE LA SIMULATION DU PAIEMENT ---");

        //3. VÉRIFICATIONS (ASSERTIONS)
        assertNotNull(transactionResultat);
        assertNotNull(transactionResultat.getId(), "L'UUID de la transaction doit être généré.");
        assertEquals(TransactionPaiement.StatutTransaction.FAILED, transactionResultat.getStatut(), 
                "Le paiement Stripe avec 'tok_chargeDeclinedInsufficientFunds' aurait dû passer au statut FAILED.");        
        System.out.println("Paiement simulé avec succès : ECHEC EN BDD !");
        System.out.println("ID Transaction généré : " + transactionResultat.getId());
        System.out.println("Statut final en Base Supabase : " + transactionResultat.getStatut());
    }
    
    @Test
    void simulerUnPaiementEchouePourCarteExpiree() {
        //1. PRÉPARATION DES DONNÉES DE SIMULATION
        UUID compteClientIdFictif = UUID.randomUUID();
        this.creerClient(compteClientIdFictif);
        int montantEnCentimes = 2500; //Simule un panier de 25,00 €
        String cleIdempotenceUnique = UUID.randomUUID().toString(); //Clé d'idempotence unique       
        //Jeton Stripe de test officiel pour simuler une carte valide
        //(Stripe intercepte ce token magique et renverra toujours 'succeeded')
        String tokenCarteStripeTest = "tok_chargeDeclinedExpiredCard";
        //2. EXÉCUTION DU CAS D'USAGE DANS L'HEXAGONE ---
        System.out.println("--- DÉBUT DE LA SIMULATION DU PAIEMENT ---");
        TransactionPaiement transactionResultat = executionPaiementUseCase.traiterPaiement(
                compteClientIdFictif,
                montantEnCentimes,
                cleIdempotenceUnique,
                tokenCarteStripeTest
        );
        System.out.println("--- FIN DE LA SIMULATION DU PAIEMENT ---");
        //3. VÉRIFICATIONS (ASSERTIONS)
        assertNotNull(transactionResultat);
        assertNotNull(transactionResultat.getId(), "L'UUID de la transaction doit être généré.");
        assertEquals(TransactionPaiement.StatutTransaction.FAILED, transactionResultat.getStatut(), 
                "Le paiement Stripe avec 'tok_chargeDeclinedExpiredCard' aurait dû passer au statut FAILED.");        
        System.out.println("Paiement simulé avec succès : ECHEC EN BDD !");
        System.out.println("ID Transaction généré : " + transactionResultat.getId());
        System.out.println("Statut final en Base Supabase : " + transactionResultat.getStatut());
    }    

    @Test
    void simulerSecuriteIdempotenceDoubleDebit() {
        UUID compteClientIdFictif = UUID.randomUUID();
        this.creerClient(compteClientIdFictif);
        int montantEnCentimes = 1500;
        String cleIdempotenceDupliquee = "CLE_UNIQUE_TEST_IDEMPOTENCE";
        String tokenCarteStripeTest = "tok_visa";
        //Premier appel : Initialise et valide
        System.out.println("--- DÉBUT DE LA SIMULATION DU PAIEMENT ---");
        TransactionPaiement premierAppel = executionPaiementUseCase.traiterPaiement(
                compteClientIdFictif, montantEnCentimes, cleIdempotenceDupliquee, tokenCarteStripeTest);

        //Deuxième appel immédiat avec LA MÊME clé d'idempotence (Simulation d'un double clic client)
        TransactionPaiement deuxiemeAppel = executionPaiementUseCase.traiterPaiement(
                compteClientIdFictif, montantEnCentimes, cleIdempotenceDupliquee, tokenCarteStripeTest);
        System.out.println("--- FIN DE LA SIMULATION DU PAIEMENT ---");      
        //Vérification : l'application n'a pas ré-exécuté le débit, elle a renvoyé la même transaction
        assertEquals(premierAppel.getId(), deuxiemeAppel.getId(), 
                "L'idempotence a échoué : deux transactions distinctes ont été créées avec la même clé !");
        System.out.println("Sécurité Idempotence validée avec succès : Aucun double débit détecté.");
    }
    
    @Test
    void simulerUneAnnulationDePaiementReussie() {
    	TransactionPaiement transactionPaiement=null;    	
    	//On dit au Spy de ne pas exécuter la vraie méthode HTTP d'annulation, mais de simuler un succès
    	Mockito.doReturn(true) 
    	       .when(stripeMokbankAdaptateur)
    	       .annulerPaiement(Mockito.anyString());  	
        UUID transactionId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        String cleIdempotence = "cle_annulation_test"+UUID.randomUUID().toString().substring(0, 8);
        String emailUnique = "test-annulation-" + clientId + "@paybank.com";
        //insertion du client (respect des contraintes des celfs etrangeres)
        jdbcTemplate.update(
                "INSERT INTO clients (id, nom, email) VALUES (?::uuid, ?, ?) ON CONFLICT (id) DO NOTHING",
                clientId, "Client Test Annulation", emailUnique
        );    
        //Création d'un vrai paiement Stripe
        transactionPaiement=executionPaiementUseCase.traiterPaiement(clientId, 10000, cleIdempotence, "tok_visa");    
                
        //Déclenchement de l'annulation
        //(Être sûr d'avoir injecté le UseCase ou le Service dans le test)
        String paymentIntentId = jdbcTemplate.queryForObject(
        		"SELECT stripe_payment_intent_id FROM paiements WHERE cle_idempotence = ?",
        	    String.class,
        	    cleIdempotence
        );
              
        annulerPaiementUseCase.executerAnnulationSurIDPaymentIntent(paymentIntentId); 
        //Vérification finale en BDD Supabase
        String statutFinal = jdbcTemplate.queryForObject(
            "SELECT statut FROM paiements WHERE id = ?::uuid",
            String.class,
            transactionPaiement.getId()
        );
        assertEquals("REFUNDED", statutFinal);
    }
    
    @Test
    void simulerUneModificationDeMontantReussie() {
        //PRÉPARATION : Création d'une transaction initiale
        UUID compteClientIdFictif = UUID.randomUUID();
        this.creerClient(compteClientIdFictif);
        
        int montantInitialCentimes = 3000; //30,00 €
        String cleIdempotenceUnique = UUID.randomUUID().toString();
        String tokenCarteStripeTest = "tok_visa"; 

        // On génère le paiement d'origine via l'hexagone
        TransactionPaiement transactionInitiale = executionPaiementUseCase.traiterPaiement(
                compteClientIdFictif,
                montantInitialCentimes,
                cleIdempotenceUnique,
                tokenCarteStripeTest
        );
         
        assertNotNull(transactionInitiale);
        UUID paiementId = transactionInitiale.getId();
        
        jdbcTemplate.update("UPDATE paiements SET statut = 'PENDING' WHERE id = ?::uuid", paiementId);
        
        //2. EXÉCUTION : Modification du montant du paiement
        int nouveauMontantBrut = 4500; //nouveau montant ciblé : 45,00 €
        MontantCentimes nouveauMontant = new MontantCentimes(nouveauMontantBrut); //Instanciation du Value Object

        System.out.println("--- DÉBUT DE LA MODIFICATION DU PAIEMENT ---");
        //Appel du Use Case configuré
        modifierPaiementUseCase.modifier(paiementId, nouveauMontant);
        System.out.println("--- FIN DE LA MODIFICATION DU PAIEMENT ---");

        //3. VÉRIFICATIONS (ASSERTIONS)
        //On va directement chercher la ligne en BDD Supabase pour valider la persistance réelle
        Integer montantFinalEnBdd = jdbcTemplate.queryForObject(
            "SELECT montant_centimes FROM paiements WHERE id = ?::uuid",
            Integer.class,
            paiementId
        );

        assertNotNull(montantFinalEnBdd, "Le paiement devrait toujours exister en BDD.");
        assertEquals(4500, montantFinalEnBdd.intValue(), 
                "Le montant en base de données aurait dû être mis à jour à 4500 centimes.");
        
        System.out.println("Modification validée avec succès !");
        System.out.println("Montant initial : " + montantInitialCentimes + " cts -> Montant final en BDD : " + montantFinalEnBdd + " cts");
    }
    
}