package edu.ifsi.examrequests.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.ifsi.examrequests.model.Course;

@Repository
public class CourseRepository {

    private static final String FIND_ALL_ACTIVE = """
            SELECT course_code, course_name
            FROM courses
            WHERE is_active = 1
            ORDER BY course_name
            """;

    private static final String FIND_ACTIVE_NAME_BY_CODE = """
            SELECT course_name
            FROM courses
            WHERE course_code = ? AND is_active = 1
            """;

    private final JdbcTemplate jdbc;

    public CourseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Course> findAllActive() {
        return jdbc.query(FIND_ALL_ACTIVE,
                (rs, rowNum) -> new Course(rs.getString("course_code"), rs.getString("course_name")));
    }

    // Empty if the code doesn't exist or the course is inactive.
    public Optional<String> findActiveNameByCode(String courseCode) {
        return jdbc.query(FIND_ACTIVE_NAME_BY_CODE, (rs, rowNum) -> rs.getString("course_name"), courseCode)
                .stream()
                .findFirst();
    }
}
