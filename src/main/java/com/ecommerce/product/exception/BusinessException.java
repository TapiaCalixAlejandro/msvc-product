package com.ecommerce.product.exception;

/**
 *  Conflicto de estado (HTTP 409).
 *  intentar crear recurso duplicado, inconsistencias de versión (optimistic locking), etc.
 */
public class BusinessException extends RuntimeException {
    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
