package edu.ifsi.examrequests.model;

public record ExamRequest(
    int requestId, int studentId, String firstName, String lastName,
    String fireDepartment, String courseCode, String courseName,
    String requestDate, String status, boolean needsAccommodation, String notes) {}

