-- ============================================================
-- IFSI Web Application Developer - Code Challenge
-- setup_sqlite.sql  (SQLite version of setup.sql: same tables and data)
-- needs_accommodation: 1 = yes, 0 = no (JSON uses true/false)
-- Several students intentionally have more than one request for the
-- same course (needed for Task 1). Do not remove them.
-- ============================================================

DROP TABLE IF EXISTS exam_requests;

DROP TABLE IF EXISTS students;

CREATE TABLE students (
  student_id INTEGER PRIMARY KEY AUTOINCREMENT,
  first_name TEXT(50) NOT NULL,
  last_name TEXT(50) NOT NULL,
  email TEXT(120) NOT NULL,
  fire_department TEXT(100) NULL
);

CREATE TABLE exam_requests (
  request_id INTEGER PRIMARY KEY AUTOINCREMENT,
  student_id INTEGER NOT NULL REFERENCES students (
    student_id
  ),
  course_code TEXT(20) NOT NULL,
  course_name TEXT(100) NOT NULL,
  request_date DATE NOT NULL,
  status TEXT(20) NOT NULL DEFAULT 'Pending',
  needs_accommodation INTEGER NOT NULL DEFAULT 0,
  notes TEXT(500) NULL
);

INSERT INTO students (
  first_name,
  last_name,
  email,
  fire_department
)
VALUES
  ('Jordan', 'Miller', 'jmiller@example.com', 'Champaign FD'),
  ('Casey', 'Nguyen', 'cnguyen@example.com', 'Urbana FD'),
  ('Riley', 'Johnson', 'rjohnson@example.com', 'Savoy FPD'),
  ('Morgan', 'Patel', 'mpatel@example.com', 'Decatur FD'),
  ('Taylor', 'Brooks', 'tbrooks@example.com', 'Danville FD'),
  ('Avery', 'Garcia', 'agarcia@example.com', 'Champaign FD'),
  ('Quinn', 'Robinson', 'qrobinson@example.com', 'Mahomet FPD'),
  ('Drew', 'Anderson', 'danderson@example.com', 'Rantoul FD'),
  ('Jamie', 'Lee', 'jlee@example.com', 'Urbana FD'),
  ('Parker', 'Wilson', 'pwilson@example.com', 'Tolono FPD');

INSERT INTO exam_requests (
  student_id,
  course_code,
  course_name,
  request_date,
  status,
  needs_accommodation,
  notes
)
VALUES
  (1, 'FF2', 'Firefighter II', '2026-08-03', 'Completed', 0, NULL),
  (
    1,
    'FF2',
    'Firefighter II',
    '2026-09-14',
    'Pending',
    0,
    'Retaking practical portion'
  ),
  (
    2,
    'FF2',
    'Firefighter II',
    '2026-09-15',
    'Pending',
    1,
    'Extended time requested'
  ),
  (3, 'HMO', 'Hazardous Materials Operations', '2026-08-20', 'Approved', 0, NULL),
  (3, 'FF2', 'Firefighter II', '2026-09-18', 'Pending', 0, NULL),
  (4, 'FAE', 'Fire Apparatus Engineer', '2026-07-11', 'Completed', 0, NULL),
  (
    4,
    'FAE',
    'Fire Apparatus Engineer',
    '2026-08-29',
    'Denied',
    0,
    'Missing prerequisite'
  ),
  (
    4,
    'FAE',
    'Fire Apparatus Engineer',
    '2026-09-20',
    'Pending',
    0,
    'Prerequisite now on file'
  ),
  (
    5,
    'HMO',
    'Hazardous Materials Operations',
    '2026-09-02',
    'Pending',
    1,
    'Reader requested'
  ),
  (6, 'FIO', 'Fire Instructor I', '2026-09-05', 'Approved', 0, NULL),
  (6, 'FF2', 'Firefighter II', '2026-09-21', 'Pending', 0, NULL),
  (7, 'FIO', 'Fire Instructor I', '2026-08-15', 'Completed', 0, NULL),
  (7, 'FIO', 'Fire Instructor I', '2026-09-22', 'Pending', 0, NULL),
  (8, 'HMO', 'Hazardous Materials Operations', '2026-09-09', 'Pending', 0, NULL),
  (
    9,
    'FAE',
    'Fire Apparatus Engineer',
    '2026-09-10',
    'Approved',
    1,
    'Separate room'
  ),
  (9, 'HMO', 'Hazardous Materials Operations', '2026-09-23', 'Pending', 0, NULL),
  (10, 'FF2', 'Firefighter II', '2026-06-30', 'Completed', 0, NULL),
  (10, 'FIO', 'Fire Instructor I', '2026-09-24', 'Pending', 0, NULL),
  (2, 'HMO', 'Hazardous Materials Operations', '2026-09-25', 'Pending', 0, NULL),
  (5, 'HMO', 'Hazardous Materials Operations', '2026-07-19', 'Completed', 0, NULL);
