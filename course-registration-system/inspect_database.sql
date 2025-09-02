-- List all programs
SELECT * FROM programs;

-- List all courses
SELECT * FROM courses;

-- List all courses
SELECT * FROM enrollments;

-- List all courses
SELECT * FROM lecture_schedules;

-- List all courses
SELECT * FROM course_programs;

-- List all users
SELECT * FROM users;

-- If you have a user_profile table that links users to programs:
SELECT * FROM user_profiles;

-- If you have a course_program join table (for many-to-many):
SELECT * FROM program_courses;

-- If you want to see which courses are linked to which programs (example for many-to-many):
SELECT
    p.id AS program_id, p.name AS program_name,
    c.id AS course_id, c.name AS course_name
FROM
    program p
    JOIN course_programs cp ON p.id = cp.program_id
    JOIN courses c ON c.id = cp.course_id
ORDER BY p.name, c.name;