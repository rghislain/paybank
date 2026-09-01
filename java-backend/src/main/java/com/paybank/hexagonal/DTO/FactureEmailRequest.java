package com.paybank.hexagonal.DTO;

import java.util.List;

public class FactureEmailRequest {
    private List<String> emails;
    private String factureDetails;

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }
    public String getFactureDetails() { return factureDetails; }
    public void setFactureDetails(String factureDetails) { this.factureDetails = factureDetails; }
}