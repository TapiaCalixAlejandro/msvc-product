package com.ecommerce.product.exception;

/**
 * Servicio temporalmente no disponible (HTTP 503).
 * cualquier excepción no manejada.
 */
public class InternalServerException extends RuntimeException {
    public InternalServerException() {
        super();
    }

    public InternalServerException(String message) {
        super(message);
    }
}
