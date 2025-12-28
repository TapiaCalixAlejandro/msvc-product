package com.ecommerce.product.exception;

import java.util.List;

/**
 *  Errores de validación específicos de negocio (HTTP 422 o 400 según política).
 *  validaciones compuestas, reglas de negocio complejas.
 * */
public class ValidationException extends RuntimeException {
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
