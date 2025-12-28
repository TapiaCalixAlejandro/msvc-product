package com.ecommerce.product.exception;

/**
 *  Error al llamar a otro microservicio (HTTP 502 / 503 según caso).
 *  cuando msvc-category responde con error o timeout. Incluye código de status remoto y mensaje.
 */
public class ExternalServiceException extends RuntimeException {
    private final int status;
    private final String serviceName;

    public ExternalServiceException(String message, int status, String serviceName) {
        super(message);
        this.status = status;
        this.serviceName = serviceName;
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
        this.status = 0;
        this.serviceName = null;
    }

    public int getStatus() {
        return status;
    }

    public String getServiceName() {
        return serviceName;
    }
}
