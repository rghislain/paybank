package com.paybank.hexagonal.domaine.model;

public record MatchResult(
    Transaction tCompta,
    Transaction tBanque,
    String statut,
    double score
) {}