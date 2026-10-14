package edu.ifsi.examrequests.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import edu.ifsi.examrequests.model.ExamRequest;
import edu.ifsi.examrequests.model.ValidatedRequest;

@Repository
public class ExamRequestRepository {

    // Shared by every request query so all endpoints return the same shape.
    private static final String SELECT_REQUESTS = """
            SELECT r.request_id, r.student_id, s.first_name, s.last_name, s.fire_department,
                   r.course_code, r.course_name, r.request_date, r.status,
                   r.needs_accommodation, r.notes
            FROM exam_requests r
            JOIN students s ON s.student_id = r.student_id
            """;

    // New requests always start as Pending; the client can't choose the status.
    private static final String INSERT = """
            INSERT INTO exam_requests
                (student_id, course_code, course_name, request_date, status, needs_accommodation, notes)
            VALUES (?, ?, ?, ?, 'Pending', ?, ?)
            """;

    private final JdbcTemplate jdbc;

    public ExamRequestRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ExamRequest> findAll() {
        return jdbc.query(SELECT_REQUESTS + " ORDER BY r.request_id", this::mapRequest);
    }

    public Optional<ExamRequest> findById(int requestId) {
        return jdbc.query(SELECT_REQUESTS + " WHERE r.request_id = ?", this::mapRequest, requestId)
                .stream()
                .findFirst();
    }

    // Returns the generated request_id.
    public int insert(ValidatedRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, request.studentId());
            ps.setString(2, request.courseCode());
            ps.setString(3, request.courseName());
            ps.setString(4, request.requestDate().toString());   // stored as YYYY-MM-DD, like the sample data
            ps.setInt(5, request.needsAccommodation() ? 1 : 0);
            ps.setString(6, request.notes());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    private ExamRequest mapRequest(ResultSet rs, int rowNum) throws SQLException {
        return new ExamRequest(
                rs.getInt("request_id"), rs.getInt("student_id"),
                rs.getString("first_name"), rs.getString("last_name"), rs.getString("fire_department"),
                rs.getString("course_code"), rs.getString("course_name"),
                rs.getString("request_date"), rs.getString("status"),
                rs.getInt("needs_accommodation") == 1,
                rs.getString("notes"));
    }
}
