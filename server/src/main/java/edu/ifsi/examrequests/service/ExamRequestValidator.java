package edu.ifsi.examrequests.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import edu.ifsi.examrequests.model.NewExamRequest;
import edu.ifsi.examrequests.model.ValidatedRequest;
import edu.ifsi.examrequests.repository.CourseRepository;
import edu.ifsi.examrequests.repository.StudentRepository;

// All server-side rules for a new exam request. Error keys match the JSON field names
// so the page can show each message under its field.
@Component
public class ExamRequestValidator {

    public static final int MAX_NOTES_LENGTH = 500;

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public ExamRequestValidator(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    // Runs every check so the user sees all problems at once.
    public ValidatedRequest validate(NewExamRequest input) {
        Map<String, String> errors = new LinkedHashMap<>();

        Integer studentId = checkStudentId(input.studentId(), errors);
        String courseCode = trimToNull(input.courseCode());
        String courseName = checkCourse(courseCode, errors);
        LocalDate requestDate = checkRequestDate(input.requestDate(), errors);
        Boolean needsAccommodation = checkAccommodation(input.needsAccommodation(), errors);
        String notes = checkNotes(input.notes(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return new ValidatedRequest(studentId, courseCode, courseName, requestDate, needsAccommodation, notes);
    }

    private Integer checkStudentId(String raw, Map<String, String> errors) {
        String value = trimToNull(raw);
        if (value == null) {
            errors.put("student_id", "Student ID is required.");
            return null;
        }
        if (!value.matches("\\d+")) {
            errors.put("student_id", "Student ID must be a whole number.");
            return null;
        }
        try {
            int id = Integer.parseInt(value);
            if (studentRepository.existsById(id)) {
                return id;
            }
        } catch (NumberFormatException tooLarge) {
            // Digits only but beyond int range: no such student either.
        }
        errors.put("student_id", "No student found with ID " + value + ".");
        return null;
    }

    // Returns the course name from the database; the client's name is never trusted.
    private String checkCourse(String courseCode, Map<String, String> errors) {
        if (courseCode == null) {
            errors.put("course_code", "Course is required.");
            return null;
        }
        Optional<String> name = courseRepository.findActiveNameByCode(courseCode);
        if (name.isEmpty()) {
            errors.put("course_code", "Choose a valid course.");
            return null;
        }
        return name.get();
    }

    private LocalDate checkRequestDate(String raw, Map<String, String> errors) {
        String value = trimToNull(raw);
        if (value == null) {
            errors.put("request_date", "Exam date is required.");
            return null;
        }
        LocalDate date;
        try {
            // ISO format is strict: 2026-02-30 is rejected, not rolled over to March.
            date = LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            errors.put("request_date", "Enter a real date as YYYY-MM-DD.");
            return null;
        }
        if (date.isBefore(LocalDate.now())) {
            errors.put("request_date", "Exam date can't be in the past.");
            return null;
        }
        return date;
    }

    // Accepts true/false or 1/0 (as JSON values or strings); missing means false.
    private Boolean checkAccommodation(Object raw, Map<String, String> errors) {
        if (raw == null) {
            return false;
        }
        if (raw instanceof Boolean flag) {
            return flag;
        }
        String value = raw.toString().trim().toLowerCase();
        switch (value) {
            case "true", "1":
                return true;
            case "false", "0":
                return false;
            default:
                errors.put("needs_accommodation", "Accommodation must be true or false.");
                return null;
        }
    }

    private String checkNotes(String raw, Map<String, String> errors) {
        String notes = trimToNull(raw);
        if (notes != null && notes.length() > MAX_NOTES_LENGTH) {
            errors.put("notes", "Notes must be " + MAX_NOTES_LENGTH + " characters or fewer.");
            return null;
        }
        return notes;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
