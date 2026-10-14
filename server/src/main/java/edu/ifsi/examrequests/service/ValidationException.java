package edu.ifsi.examrequests.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// Thrown when a submission fails validation; errors map field name -> message.
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Validation failed: " + errors);
        // Keep the form's field order (Map.copyOf would lose it).
        this.errors = Collections.unmodifiableMap(new LinkedHashMap<>(errors));
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
