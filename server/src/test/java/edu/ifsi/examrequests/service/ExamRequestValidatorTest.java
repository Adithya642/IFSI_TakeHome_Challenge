package edu.ifsi.examrequests.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.ifsi.examrequests.model.NewExamRequest;
import edu.ifsi.examrequests.model.ValidatedRequest;
import edu.ifsi.examrequests.repository.CourseRepository;
import edu.ifsi.examrequests.repository.StudentRepository;

class ExamRequestValidatorTest {

    private static final String FUTURE_DATE =
            LocalDate.now(ZoneId.of("America/Chicago")).plusDays(30).toString();

    private ExamRequestValidator validator;

    // Fake repositories: student 4 and course FF2 exist; anything else doesn't
    // (Mockito returns false / Optional.empty() for calls we haven't set up).
    @BeforeEach
    void setUp() {
        StudentRepository studentRepository = mock(StudentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        when(studentRepository.existsById(4)).thenReturn(true);
        when(courseRepository.findActiveNameByCode("FF2")).thenReturn(Optional.of("Firefighter II"));
        validator = new ExamRequestValidator(studentRepository, courseRepository);
    }

    @Test
    void acceptsValidRequest() {
        NewExamRequest input = new NewExamRequest("4", "FF2", FUTURE_DATE, 1, "  Retaking practical  ");

        ValidatedRequest result = validator.validate(input);

        assertThat(result.studentId()).isEqualTo(4);
        assertThat(result.courseName()).isEqualTo("Firefighter II");   // from the database, not the client
        assertThat(result.requestDate()).isEqualTo(LocalDate.parse(FUTURE_DATE));
        assertThat(result.needsAccommodation()).isTrue();              // 1 means true
        assertThat(result.notes()).isEqualTo("Retaking practical");    // trimmed
    }

    @Test
    void reportsAllMissingRequiredFields() {
        NewExamRequest input = new NewExamRequest(null, null, null, null, null);

        Map<String, String> errors = assertThrows(ValidationException.class,
                () -> validator.validate(input)).getErrors();

        assertThat(errors).containsExactly(
                Map.entry("student_id", "Student ID is required."),
                Map.entry("course_code", "Course is required."),
                Map.entry("request_date", "Exam date is required."));
    }

    @Test
    void rejectsInvalidValues() {
        String tooLongNotes = "x".repeat(ExamRequestValidator.MAX_NOTES_LENGTH + 1);
        NewExamRequest input = new NewExamRequest("abc", "XYZ", "2026-02-30", "maybe", tooLongNotes);

        Map<String, String> errors = assertThrows(ValidationException.class,
                () -> validator.validate(input)).getErrors();

        assertThat(errors).containsExactly(
                Map.entry("student_id", "Student ID must be a whole number."),
                Map.entry("course_code", "Choose a valid course."),
                Map.entry("request_date", "Enter a real date as YYYY-MM-DD."),
                Map.entry("needs_accommodation", "Accommodation must be true or false."),
                Map.entry("notes", "Notes must be " + ExamRequestValidator.MAX_NOTES_LENGTH
                        + " characters or fewer."));
    }
}
