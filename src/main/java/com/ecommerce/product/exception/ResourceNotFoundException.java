package com.ecommerce.product.exception;

/**
 *  Recurso no encontrado (HTTP 404).
 *  cuando un Product o entidad local no existe, o cuando una relación no se encuentra.
 * */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
