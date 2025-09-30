package com.example.PromptShieldAPI.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler global de exceções para tratamento centralizado de erros
 * Esta classe é crítica pois define como a aplicação responde a erros
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções de status HTTP personalizadas
     * Converte exceções ResponseStatusException em respostas HTTP apropriadas
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException ex) {
        ApiError error = new ApiError(
                ex.getStatusCode().value(),
                ex.getReason(),
                List.of()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    /**
     * Trata erros de validação de dados de entrada
     * Converte erros de validação em respostas estruturadas para o cliente
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error In The Provided Fields.",
                erros
        );

        return ResponseEntity.badRequest().body(error);
    }
}
