package com.sportstore.service;

/**
 * Excepción de negocio para errores de validación o reglas del sistema.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
