package com.ecommerce.product.exception;

import com.ecommerce.product.models.dtos.ApiResponse;
import com.ecommerce.product.models.dtos.ValidationError;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manejo global de excepciones REST.
 * Encapsula las respuestas en ApiResponse y captura información útil (traceId).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRACE_ID_KEY = "traceId";

    private ApiResponse buildResponse(HttpStatus status, String error, String message, String path, List<ValidationError> validationErrors) {
        ApiResponse api = new ApiResponse();
        api.setTimestamp(Instant.now());
        api.setStatus(status.value());
        api.setError(error);
        api.setMessage(message);
        api.setPath(path);
        api.setValidationErrors(validationErrors);
        /* Recoger traceId del MDC si existe (Se establece en un filter) */
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId != null) {
            // Asumiendo que ApiResponse tiene este campo. Si no, agrégalo al DTO.
            api.setTraceId(traceId);
        }
        return api;
    }

    // 400 - Validation errors for @RequestPart (JSON en multipart/form-data)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex, WebRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        ex.getBeanResults().forEach(result -> {
            result.getResolvableErrors().forEach(err -> {
                String paramName = result.getMethodParameter().getParameterName();
                String fieldName = paramName;

                // Si es un FieldError, construimos el path completo (ej. product.name)
                if (err instanceof FieldError fieldError) {
                    fieldName = paramName + "." + fieldError.getField();
                }

                // Manejo SEGURO del valor rechazado (puede ser null).
                Object rejectedValue = (err instanceof FieldError fe) ? fe.getRejectedValue() : null;

                errors.add(new ValidationError(fieldName, err.getDefaultMessage(), rejectedValue));
            });
        });

        log.warn("Validación de método fallida (Multipart): {}", errors);
        ApiResponse api = buildResponse(HttpStatus.BAD_REQUEST, "Validación Fallida", "Error en los parámetros enviados", request.getDescription(false), errors);
        return new ResponseEntity<>(api, HttpStatus.BAD_REQUEST);
    }

    // 400 - Bean Validation errors from @Valid (JSON @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<ValidationError> errors =  ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .collect(Collectors.toList());
        log.warn("Validación fallida en request (JSON): {}", errors); // Loguear advertencia
        ApiResponse api = buildResponse(HttpStatus.BAD_REQUEST, "Validacion Fallida", "Errores en los datos enviados", request.getDescription(false), errors);
        return new ResponseEntity<>(api, HttpStatus.BAD_REQUEST);
    }

    // 400 - Constraint violations (e.g. @RequestParam @Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String rawPath = cv.getPropertyPath().toString();
                    String fieldName = rawPath.contains(".") ? rawPath.substring(rawPath.lastIndexOf(".") + 1) : rawPath;
                    return new ValidationError(fieldName, cv.getMessage(), cv.getInvalidValue());
                })
                .collect(Collectors.toList());
        ApiResponse api = buildResponse(HttpStatus.BAD_REQUEST, "Violación de Restricción", "Parámetros inválidos", request.getDescription(false), errors);
        return new ResponseEntity<>(api, HttpStatus.BAD_REQUEST);
    }

    // 400 - malformed JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        // ApiResponse api = buildResponse(HttpStatus.BAD_REQUEST, "JSON malformado", ex.getMostSpecificCause().getMessage(), request.getDescription(false), null);
        // OJO: getMostSpecificCause() puede exponer detalles técnicos. Usar con cuidado.
        ApiResponse api = buildResponse(HttpStatus.BAD_REQUEST, "JSON Inválido", "El formato del cuerpo de la petición es incorrecto", request.getDescription(false), null);
        return new ResponseEntity<>(api, HttpStatus.BAD_REQUEST);
    }

    // 400 - type mismatch (e.g., Long expected but String received)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String msg = String.format("El parametro '%s' no se pudo convertir al tipo requerido", ex.getName());
        ApiResponse api = buildResponse(HttpStatus.BAD_REQUEST, "Desajuste de tipos", ex.getMessage(), request.getDescription(false), null);
        return new ResponseEntity<>(api, HttpStatus.BAD_REQUEST);
    }

    // 404 - resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        // Loguear como info/warn, no error, ya que es flujo "normal"
        log.info("Recurso no encontrado: {}", ex.getMessage());
        ApiResponse api = buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request.getDescription(false), null);
        return new ResponseEntity<>(api, HttpStatus.NOT_FOUND);
    }

    // 409 - conflict
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse> handleBusiness(BusinessException ex, WebRequest request) {
        log.warn("Excepción de negocio: {}", ex.getMessage());
        ApiResponse api = buildResponse(HttpStatus.CONFLICT, "Conflicto", ex.getMessage(), request.getDescription(false), null);
        return  new ResponseEntity<>(api, HttpStatus.CONFLICT);
    }

    // 422 - validation business (opcional: mapear a 422 o 400 según política)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidation(ValidationException ex, WebRequest request) {
        List<ValidationError> errors = new ArrayList<>();
        if (ex.getErrors() != null) {
            ex.getErrors().forEach(e -> errors.add(new ValidationError("negocio", e, null)));
        }
        ApiResponse api = buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio", "No se pudo procesar la entidad", request.getDescription(false), errors);
        return new ResponseEntity<>(api, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // 502/503 - external service error
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse> handleExternalService(ExternalServiceException ex, WebRequest request) {
        log.error("Fallo en servicio externo [{}]: {}", ex.getServiceName(), ex.getMessage());
        String message = String.format("Error de comunicación con servicio externo: %s", ex.getServiceName());
        ApiResponse api = buildResponse(HttpStatus.BAD_GATEWAY, "Error de servicio externo", message, request.getDescription(false), null);
        return new ResponseEntity<>(api, HttpStatus.BAD_GATEWAY);
    }

    // 503 - when circuit breaker opens or service unavailable
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiResponse> handleServiceUnavailable(ServiceUnavailableException ex, WebRequest request) {
        log.error("Servicio no disponible: {}", ex.getMessage());
        ApiResponse api = buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Servicio no disponible", ex.getMessage(), request.getDescription(false), null);
        return new ResponseEntity<>(api, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // 500 - fallback generic
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleAll(Exception ex, WebRequest request) {
        log.error("Excepción no controlada (500): {}", ex.getMessage());
        // [SEGURIDAD] Al cliente le enviamos un mensaje genérico para no exponer vulnerabilidades
        String safeMessage = "Ocurrió un error interno inesperado. Por favor contacte al administrador.";
        ApiResponse api = buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", safeMessage, request.getDescription(false), null);
        return new ResponseEntity<>(api, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
