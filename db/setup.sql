/* ============================================================
   IFSI Web Application Developer - Code Challenge
   setup.sql  (written for Microsoft SQL Server / SQL Server Express)
   Creates two tables and loads sample data.
   A SQLite version is provided in setup_sqlite.sql.
   For MySQL or PostgreSQL, adjust data types (IDENTITY, BIT, NVARCHAR) as needed.
   Note: several students intentionally have more than one request
   for the same course (needed for Task 1). Do not remove them.
   ============================================================ */

-- Requires SQL Server 2016 or later (SQL Server Express is fine)
DROP TABLE IF EXISTS dbo.exam_requests;
DROP TABLE IF EXISTS dbo.students;

CREATE TABLE dbo.students (
    student_id      INT IDENTITY(1,1) PRIMARY KEY,
    first_name      NVARCHAR(50)  NOT NULL,
    last_name       NVARCHAR(50)  NOT NULL,
    email           NVARCHAR(120) NOT NULL,
    fire_department NVARCHAR(100) NULL
);

CREATE TABLE dbo.exam_requests (
    request_id          INT IDENTITY(1,1) PRIMARY KEY,
    student_id          INT           NOT NULL REFERENCES dbo.students(student_id),
    course_code         NVARCHAR(20)  NOT NULL,
    course_name         NVARCHAR(100) NOT NULL,
    request_date        DATE          NOT NULL,
    status              NVARCHAR(20)  NOT NULL DEFAULT 'Pending',  -- Pending, Approved, Denied, Completed
    needs_accommodation BIT           NOT NULL DEFAULT 0,          -- 1 = yes, 0 = no (JSON uses true/false)
    notes               NVARCHAR(500) NULL
);

INSERT INTO dbo.students (first_name, last_name, email, fire_department) VALUES
('Jordan','Miller','jmiller@example.com','Champaign FD'),
('Casey','Nguyen','cnguyen@example.com','Urbana FD'),
('Riley','Johnson','rjohnson@example.com','Savoy FPD'),
('Morgan','Patel','mpatel@example.com','Decatur FD'),
('Taylor','Brooks','tbrooks@example.com','Danville FD'),
('Avery','Garcia','agarcia@example.com','Champaign FD'),
('Quinn','Robinson','qrobinson@example.com','Mahomet FPD'),
('Drew','Anderson','danderson@example.com','Rantoul FD'),
('Jamie','Lee','jlee@example.com','Urbana FD'),
('Parker','Wilson','pwilson@example.com','Tolono FPD');

INSERT INTO dbo.exam_requests (student_id, course_code, course_name, request_date, status, needs_accommodation, notes) VALUES
(1,'FF2','Firefighter II','2026-08-03','Completed',0,NULL),
(1,'FF2','Firefighter II','2026-09-14','Pending',0,'Retaking practical portion'),
(2,'FF2','Firefighter II','2026-09-15','Pending',1,'Extended time requested'),
(3,'HMO','Hazardous Materials Operations','2026-08-20','Approved',0,NULL),
(3,'FF2','Firefighter II','2026-09-18','Pending',0,NULL),
(4,'FAE','Fire Apparatus Engineer','2026-07-11','Completed',0,NULL),
(4,'FAE','Fire Apparatus Engineer','2026-08-29','Denied',0,'Missing prerequisite'),
(4,'FAE','Fire Apparatus Engineer','2026-09-20','Pending',0,'Prerequisite now on file'),
(5,'HMO','Hazardous Materials Operations','2026-09-02','Pending',1,'Reader requested'),
(6,'FIO','Fire Instructor I','2026-09-05','Approved',0,NULL),
(6,'FF2','Firefighter II','2026-09-21','Pending',0,NULL),
(7,'FIO','Fire Instructor I','2026-08-15','Completed',0,NULL),
(7,'FIO','Fire Instructor I','2026-09-22','Pending',0,NULL),
(8,'HMO','Hazardous Materials Operations','2026-09-09','Pending',0,NULL),
(9,'FAE','Fire Apparatus Engineer','2026-09-10','Approved',1,'Separate room'),
(9,'HMO','Hazardous Materials Operations','2026-09-23','Pending',0,NULL),
(10,'FF2','Firefighter II','2026-06-30','Completed',0,NULL),
(10,'FIO','Fire Instructor I','2026-09-24','Pending',0,NULL),
(2,'HMO','Hazardous Materials Operations','2026-09-25','Pending',0,NULL),
(5,'HMO','Hazardous Materials Operations','2026-07-19','Completed',0,NULL);
