package com.paybank.hexagonal.domaine.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.printing.PDFPageable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.paybank.hexagonal.domaine.FinancialReport;
import com.paybank.hexagonal.domaine.TransactionDetail;
import com.paybank.hexagonal.domaine.annotation.SecuredPermission;
import com.paybank.hexagonal.port.FinancialReportSPI;
import com.paybank.hexagonal.port.TransactionRepositorySPI;
import com.paybank.hexagonal.repository.TransactionRepository;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.Loader;

@Service
public class FinancialReportImpressionService implements FinancialReportSPI {

    private final TransactionRepositorySPI transRepo;
    private final JavaMailSender mailSender;

    public FinancialReportImpressionService(TransactionRepositorySPI transRepo, JavaMailSender mailSender) {
        this.transRepo = transRepo;
        this.mailSender = mailSender;
    }

    @Override
    //@SecuredPermission(ressource = "bilan", action = "lire")
    public FinancialReport generateReport(UUID clientId, LocalDate start, LocalDate end) {
        List<TransactionDetail> transactions = transRepo.findTransactionsByDateRange(clientId, start, end);
        BigDecimal debits = transactions.stream().filter(TransactionDetail::isDebit).map(TransactionDetail::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credits = transactions.stream().filter(t -> !t.isDebit()).map(TransactionDetail::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalBalance = credits.subtract(debits);
        return new FinancialReport(start, end, debits, credits, finalBalance, transactions);
    }

    @Override
    //@SecuredPermission(ressource = "rapports_financiers", action = "creer")
    public void issueInvoice(UUID clientId, UUID transactionId) {
        TransactionDetail transaction =  transRepo.findById(transactionId)
        	    .orElseThrow(() -> new IllegalArgumentException("Transaction introuvable : " + transactionId)); //transRepo.findTransactionById(transactionId)
            //.orElseThrow(() -> new IllegalArgumentException("Transaction introuvable : " + transactionId));

        try {
            // 1. Générer le PDF en mémoire
            byte[] pdfBytes = generatePdfBytes(clientId, transaction);
            String fileName = "facture-" + transactionId + ".pdf";

            // 2. Envoyer aux 3 adresses e-mail
            String[] destinataires = {
                "client@paybank.com", 
                "comptabilite@paybank.com", 
                "direction@paybank.com"
            };
            sendEmailWithPdf(destinataires, pdfBytes, fileName);

            // 3. Imprimer automatiquement le document sur l'imprimante par défaut du serveur
            printPdf(pdfBytes);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du traitement de la facture : " + e.getMessage(), e);
        }
    }

    private byte[] generatePdfBytes(UUID clientId, TransactionDetail tx) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                //contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("PAYBANK - FACTURE OFFICIELLE");

                //contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(0, -40);
                contentStream.showText("Client ID : " + clientId);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Transaction ID : " + tx.id());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Montant : " + tx.amount() + " EUR");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Date : " + LocalDate.now());
                contentStream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void sendEmailWithPdf(String[] toEmails, byte[] pdfBytes, String fileName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmails);
        helper.setSubject("Votre Facture PayBank - Émission Automatique");
        helper.setText("Bonjour,\n\nVeuillez trouver ci-joint la facture relative à votre transaction récente.\n\nCordialement,\nL'équipe Paybank.");
        helper.addAttachment(fileName, new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }

    /*
    private void printPdf(byte[] pdfBytes) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(document));
            
            PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultService != null) {
                job.setPrintService(defaultService);
                job.print();
            } else {
                System.err.println("Aucune imprimante par défaut détectée sur le serveur.");
            }
        } catch (Exception e) {
            System.err.println("Échec de l'impression physique : " + e.getMessage());
        }
    }
    */
    
    

    private void printPdf(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(document));
            
            PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
            if (defaultService != null) {
                job.setPrintService(defaultService);
                job.print();
            } else {
                System.err.println("Aucune imprimante par défaut détectée sur le serveur.");
            }
        } catch (Exception e) {
            System.err.println("Échec de l'impression physique : " + e.getMessage());
        }
    }
    
    
}