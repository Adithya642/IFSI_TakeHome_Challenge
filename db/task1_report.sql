-- Task 1 reports. Standard SQL on the kit tables only (runs on SQLite and SQL Server).
-- Run: sqlite3 -header -column db/ifsi.db < db/task1_report.sql

-- Query 1: pending requests per course.
-- CASE instead of WHERE so courses with no pending requests still show 0.
SELECT course_code,
       course_name,
       SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END) AS pending_count
FROM exam_requests
GROUP BY course_code, course_name
ORDER BY pending_count DESC, course_code;
-- Expected: FF2 4, HMO 4, FIO 2, FAE 1


-- Query 2: students with more than one request for the same course.
SELECT s.student_id,
       s.first_name,
       s.last_name,
       r.course_code,
       r.course_name,
       COUNT(*) AS request_count
FROM exam_requests r
JOIN students s ON s.student_id = r.student_id
GROUP BY s.student_id, s.first_name, s.last_name, r.course_code, r.course_name
HAVING COUNT(*) > 1
ORDER BY request_count DESC, s.last_name, r.course_code;
-- Expected: Patel 3, Brooks 2, Miller 2, Robinson 2
