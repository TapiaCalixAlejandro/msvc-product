package com.ecommerce.product.exception;

/**
 *  Servicio temporalmente no disponible (HTTP 503).
 *  cuando circuit breaker está abierto o fallback no disponible.
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException() {
        super();
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
