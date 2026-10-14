-- Course catalog. Not part of the kit's setup files; the server creates it on startup.
-- Safe to run on every start: IF NOT EXISTS and INSERT OR IGNORE never duplicate.
CREATE TABLE IF NOT EXISTS courses (
    course_code TEXT    PRIMARY KEY NOT NULL,
    course_name TEXT    NOT NULL,
    is_active   INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

INSERT OR IGNORE INTO courses (course_code, course_name) VALUES
    ('FF2', 'Firefighter II'),
    ('HMO', 'Hazardous Materials Operations'),
    ('FIO', 'Fire Instructor I'),
    ('FAE', 'Fire Apparatus Engineer');
