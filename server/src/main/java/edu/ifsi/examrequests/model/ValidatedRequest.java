package edu.ifsi.examrequests.model;

import java.time.LocalDate;

public record ValidatedRequest(
        int studentId,
        String courseCode,
        String courseName,
        LocalDate requestDate,
        boolean needsAccommodation,
        String notes) {}
