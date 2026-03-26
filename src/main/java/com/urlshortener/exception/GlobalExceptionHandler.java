package com.urlshortener.exception;

import com.urlshortener.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — URL not found
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotFound(
            UrlNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(404)
                        .error("URL_NOT_FOUND")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    // 409 — Alias already taken
    @ExceptionHandler(AliasAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAliasExists(
            AliasAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .status(409)
                        .error("ALIAS_ALREADY_EXISTS")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    // 410 — URL expired (410 Gone is perfect for this)
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUrlExpired(
            UrlExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(ErrorResponse.builder()
                        .status(410)
                        .error("URL_EXPIRED")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    // 400 — Validation failures (@Valid annotation failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(400)
                        .error("VALIDATION_FAILED")
                        .message(message)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    // 500 — Catch-all for anything unexpected
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(500)
                        .error("INTERNAL_SERVER_ERROR")
                        .message("Something went wrong. Please try again.")
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }
}