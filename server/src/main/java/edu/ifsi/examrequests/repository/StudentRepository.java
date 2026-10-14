package edu.ifsi.examrequests.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository {

    private static final String COUNT_BY_ID = "SELECT COUNT(*) FROM students WHERE student_id = ?";

    private final JdbcTemplate jdbc;

    public StudentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsById(int studentId) {
        Integer count = jdbc.queryForObject(COUNT_BY_ID, Integer.class, studentId);
        return count != null && count > 0;
    }
}
