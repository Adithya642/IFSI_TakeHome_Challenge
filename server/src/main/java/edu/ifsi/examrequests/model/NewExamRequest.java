package edu.ifsi.examrequests.model;

// Raw POST body. Loose types on purpose: bad input reaches the validator
// and gets a clear message instead of failing inside the JSON parser.
public record NewExamRequest(
        String studentId,
        String courseCode,
        String requestDate,
        Object needsAccommodation,
        String notes) {}
