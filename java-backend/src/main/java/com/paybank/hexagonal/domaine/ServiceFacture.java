package com.paybank.hexagonal.domaine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.paybank.hexagonal.entity.FactureEntity;
import com.paybank.hexagonal.repository.SpringDataFactureRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.mail.internet.MimeMessage;

/**
 * Génère et envoie les factures au format PDF (Apache PDFBox).
 *
 * Les données du paiement sont lues directement chez Stripe via PaymentIntent.retrieve(),
 * ce qui évite de dépendre d'une entité locale "Paiement" dont je n'ai pas la structure exacte.
 *
 * ⚠️ Hypothèses à vérifier :
 *    - Client possède bien getNom() et getEmail().
 *    - PDFBox 3.x (confirmé via pom.xml) : les polices standard s'instancient désormais via
 *      "new PDType1Font(Standard14Fonts.FontName.HELVETICA)" au lieu des anciennes constantes
 *      statiques HELVETICA (supprimées depuis PDFBox 3.0).
 */
@Service
public class ServiceFacture {

    private static final PDType1Font HELVETICA = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font HELVETICA_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final ServiceGestionClient serviceGestionClient;
    private final OperateurCourantService operateurCourantService;
    private final JavaMailSender javaMailSender;

    // 👇 À renseigner dans application.properties avec les vraies adresses souhaitées.
    @Value("${facture.email.compta:ghislainrochette44@gmail.com}") //compta@paybank.com
    private String emailCompta;

    @Value("${facture.email.direction:ghislainrochette44@gmail.com}") //direction@paybank.com
    private String emailDirection;
    
    private final SpringDataFactureRepository factureRepository; // 👈 Injection du Repository pour la BDD

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Europe/Paris"));

    private static final float MARGE = 50;

    public ServiceFacture(ServiceGestionClient serviceGestionClient,
                           OperateurCourantService operateurCourantService,
                           JavaMailSender javaMailSender, SpringDataFactureRepository springDataFactureRepository) {
        this.serviceGestionClient = serviceGestionClient;
        this.operateurCourantService = operateurCourantService;
        this.javaMailSender = javaMailSender;
		this.factureRepository = springDataFactureRepository;
    }

    /** Génère la facture au format PDF (tableau de bytes) pour affichage/impression/téléchargement. */
    public byte[] genererFacturePdf(String paiementId, UUID clientId) throws StripeException, IOException {
        PaymentIntent intent = PaymentIntent.retrieve(paiementId.toString());
        Client client = serviceGestionClient.obtenirClient(clientId, operateurCourantService.getRoleConnecte().name());

        long montantCentimes = intent.getAmount();
        String devise = intent.getCurrency() != null ? intent.getCurrency().toUpperCase() : "EUR";
        String statut = intent.getStatus();
        String dateFormatee = FORMAT_DATE.format(Instant.ofEpochSecond(intent.getCreated()));
        String montantFormate = String.format("%,.2f", montantCentimes / 100.0).replace(',', ' ').replace('.', ',');

        sauvegarderFactureEnBdd(paiementId, client, montantCentimes, devise, statut);
        return construirePdf(paiementId, client, montantFormate, devise, statut, dateFormatee);
    }
    
    private void sauvegarderFactureEnBdd(String paiementId, Client client, long montantCentimes, String devise, String statut) {
        // Vérifiez si la facture existe déjà pour éviter les doublons lors d'un nouvel envoi
        if (!factureRepository.existsByPaiementsId(paiementId)) {
            FactureEntity facture = new FactureEntity();
            facture.setPaiementId(paiementId);
            facture.setClientId(client.getId());
            facture.setClientNom(client.getNom());
            facture.setClientEmail(client.getEmail());
            facture.setMontantTotal(BigDecimal.valueOf(montantCentimes / 100));
            facture.setDevise(devise);
            facture.setStatut(statut);
            facture.setDateEmission(Instant.now());
            
            factureRepository.save(facture);
        }
    }

    /** Génère la facture PDF et l'envoie par email au client + aux 2 adresses fixes (compta/direction). */
    public void envoyerFactureParEmail(String paiementId, UUID clientId) throws Exception {
        Client client = serviceGestionClient.obtenirClient(clientId, operateurCourantService.getRoleConnecte().name());
        byte[] pdf = genererFacturePdf(paiementId, clientId);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(new String[] { client.getEmail(), emailCompta, emailDirection });
        helper.setSubject("Facture - Transaction " + paiementId);
        helper.setText("Veuillez trouver ci-joint la facture pour la transaction " + paiementId + ".", false);
        helper.addAttachment("facture-" + paiementId + ".pdf", new ByteArrayResource(pdf));

        javaMailSender.send(message);
    }

    private byte[] construirePdf(String paiementId, Client client, String montantFormate,
                                  String devise, String statut, String dateFormatee) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float largeurUtile = page.getMediaBox().getWidth() - 2 * MARGE;
            float y = page.getMediaBox().getHeight() - MARGE;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {

                // Titre
                cs.setFont(HELVETICA_BOLD, 22);
                ecrireTexte(cs, MARGE, y, "FACTURE");
                y -= 45;

                // Bloc référence / date
                cs.setFont(HELVETICA, 11);
                y = ecrireLigne(cs, y, "Référence : " + paiementId);
                y = ecrireLigne(cs, y, "Date : " + dateFormatee);
                y -= 15;

                // Bloc client
                y = ecrireLigne(cs, y, "Client : " + client.getNom());
                y = ecrireLigne(cs, y, "Email : " + client.getEmail());
                y -= 25;

                // En-tête du tableau
                float col1 = MARGE, col2 = MARGE + 300, col3 = MARGE + 420;

                cs.setFont(HELVETICA_BOLD, 10);
                ecrireTexte(cs, col1, y, "Description");
                ecrireTexte(cs, col2, y, "Statut");
                ecrireTexte(cs, col3, y, "Montant");

                cs.setLineWidth(0.5f);
                cs.moveTo(MARGE, y - 6);
                cs.lineTo(MARGE + largeurUtile, y - 6);
                cs.stroke();

                // Ligne de détail
                y -= 26;
                cs.setFont(HELVETICA, 10);
                ecrireTexte(cs, col1, y, "Transaction PayBank");
                ecrireTexte(cs, col2, y, statut);
                ecrireTexte(cs, col3, y, montantFormate + " " + devise);

                cs.moveTo(MARGE, y - 10);
                cs.lineTo(MARGE + largeurUtile, y - 10);
                cs.stroke();

                // Total
                y -= 45;
                cs.setFont(HELVETICA_BOLD, 14);
                ecrireTexte(cs, MARGE + largeurUtile - 180, y, "Total : " + montantFormate + " " + devise);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }

    /** Écrit une ligne de texte et retourne la position Y suivante (décalée vers le bas). */
    private float ecrireLigne(PDPageContentStream cs, float y, String texte) throws IOException {
        ecrireTexte(cs, MARGE, y, texte);
        return y - 18;
    }

    private void ecrireTexte(PDPageContentStream cs, float x, float y, String texte) throws IOException {
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(texte);
        cs.endText();
    }
    
}