package edu.ifsi.examrequests.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import edu.ifsi.examrequests.service.ValidationException;

// Every bad submission gets the same 400 shape: {"errors": {field: message}}.
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Map<String, String>> handleValidation(ValidationException e) {
        return Map.of("errors", e.getErrors());
    }

    // Broken JSON (or a field of the wrong JSON shape) fails before our validator runs.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException e) {
        return Map.of("errors", Map.of("body", "Request body must be valid JSON."));
    }
}
