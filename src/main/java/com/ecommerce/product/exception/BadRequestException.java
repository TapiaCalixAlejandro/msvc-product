package com.ecommerce.product.exception;

/**
 *  Petición mal formada o violación de reglas de negocio simples (HTTP 400)
 *  datos inválidos detectados en la lógica de servicio (más allá de validación bean).
 * */
public class BadRequestException extends RuntimeException {
    public BadRequestException() {
        super();
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
