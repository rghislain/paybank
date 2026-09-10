package com.paybank.hexagonal.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralise la conversion des SecurityException (levées notamment par DroitAspect et
 * OperateurCourantService) en réponses HTTP 403 propres, pour tous les contrôleurs.
 *
 * Nécessaire car une SecurityException levée par un @Before AOP (avant l'entrée dans la
 * méthode du contrôleur) n'est PAS interceptée par un try/catch situé à l'intérieur de
 * cette méthode.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> gererAccesRefuse(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
    }
}