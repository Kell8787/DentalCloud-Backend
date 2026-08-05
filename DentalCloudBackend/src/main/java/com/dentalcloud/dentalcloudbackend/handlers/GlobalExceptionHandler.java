package com.dentalcloud.dentalcloudbackend.handlers;

import com.dentalcloud.dentalcloudbackend.domain.dto.ApiErrorResponse;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ConflictException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.security.TraceIdFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthentication(HttpServletRequest request, Exception ex) {
        return error(request, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Las credenciales no son válidas.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(HttpServletRequest request,
                                                              MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return error(request, HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR",
                "Uno o más campos no son válidos.", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(HttpServletRequest request,
                                                                       ConstraintViolationException ex) {
        return error(request, HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR",
                "Uno o más campos no son válidos.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpServletRequest request,
                                                                      HttpMessageNotReadableException ex) {
        return error(request, HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "El cuerpo de la petición es inválido o falta un campo requerido.");
    }

    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(HttpServletRequest request, Exception ex) {
        return error(request, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(HttpServletRequest request, BusinessException ex) {
        return error(request, HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(HttpServletRequest request, ConflictException ex) {
        return error(request, HttpStatus.CONFLICT, ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(HttpServletRequest request,
                                                                           DataIntegrityViolationException ex) {
        return error(request, HttpStatus.CONFLICT, "UNIQUE_CONSTRAINT_VIOLATION",
                "Ya existe un registro con esos datos. Verifica los campos únicos.");
    }

    @ExceptionHandler({OptimisticLockException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ApiErrorResponse> handleOptimisticLock(HttpServletRequest request, Exception ex) {
        return error(request, HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION",
                "El registro cambió mientras se procesaba la operación. Recarga e inténtalo de nuevo.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRequestError(HttpServletRequest request, RuntimeException ex) {
        return error(request, HttpStatus.BAD_REQUEST, "REQUEST_INVALID", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(HttpServletRequest request, Exception ex) {
        String traceId = traceId(request);
        log.error("Error no controlado traceId={}", traceId, ex);
        return error(request, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Ocurrió un error inesperado.");
    }

    private ResponseEntity<ApiErrorResponse> error(HttpServletRequest request,
                                                   HttpStatus status,
                                                   String code,
                                                   String message) {
        return error(request, status, code, message, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> error(HttpServletRequest request,
                                                   HttpStatus status,
                                                   String code,
                                                   String message,
                                                   Map<String, String> fieldErrors) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .status(status.value())
                .code(code)
                .message(message == null ? status.getReasonPhrase() : message)
                .fieldErrors(fieldErrors)
                .traceId(traceId(request))
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    private String traceId(HttpServletRequest request) {
        Object value = request.getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
        return value == null ? "unknown" : value.toString();
    }
}
