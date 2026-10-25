
USE kec_academic;

-- 1. Add courses
INSERT IGNORE INTO courses
(course_code, title, credits, course_type)
VALUES
('24ITC31', 'Full Stack Development', 4, 'THEORY'),
('24ITC32', 'Database Management Systems', 4, 'THEORY'),
('24ITC33', 'Java Programming', 4, 'THEORY'),
('24ITC34', 'Mathematics', 3, 'THEORY'),
('24ITC35', 'Computer Networks', 3, 'THEORY');

-- 2. Create 60 students: 25ITR001 to 25ITR060
INSERT IGNORE INTO students
(roll_no, student_name, department, current_semester)
WITH RECURSIVE numbers AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < 60
)
SELECT
    CONCAT('25ITR', LPAD(n, 3, '0')),
    CONCAT('Student ', LPAD(n, 3, '0')),
    'IT',
    3
FROM numbers;

-- 3. Enroll all 60 students in five courses
INSERT IGNORE INTO enrollments
(roll_no, course_code, semester, total_classes,
 classes_attended, medical_condonation)
SELECT
    s.roll_no,
    c.course_code,
    3,
    40,
    25 + MOD(CRC32(CONCAT(s.roll_no, c.course_code)), 16),
    FALSE
FROM students s
CROSS JOIN courses c
WHERE s.roll_no BETWEEN '25ITR001' AND '25ITR060'
  AND c.course_code IN (
    '24ITC31', '24ITC32', '24ITC33',
    '24ITC34', '24ITC35'
  );

-- 4. Add sample CIA and ESE marks
INSERT IGNORE INTO assessment_marks
(enrollment_id, cia1, cia2, assignment_mark,
 ese_mark, ese_absent)
SELECT
    e.enrollment_id,
    50 + MOD(CRC32(CONCAT(e.roll_no, 'CIA1')), 46),
    50 + MOD(CRC32(CONCAT(e.roll_no, 'CIA2')), 46),
    50 + MOD(CRC32(CONCAT(e.roll_no, 'ASSIGN')), 46),
    50 + MOD(CRC32(CONCAT(e.roll_no, 'ESE')), 46),
    FALSE
FROM enrollments e
WHERE e.semester = 3
  AND e.roll_no BETWEEN '25ITR001' AND '25ITR060'
  AND e.course_code IN (
    '24ITC31', '24ITC32', '24ITC33',
    '24ITC34', '24ITC35'
  );

-- 5. Verify student count
SELECT COUNT(*) AS total_students
FROM students
WHERE roll_no BETWEEN '25ITR001' AND '25ITR060';

-- 6. Verify enrollment count
SELECT COUNT(*) AS total_enrollments
FROM enrollments
WHERE roll_no BETWEEN '25ITR001' AND '25ITR060'
  AND semester = 3;

-- 7. Display all 60 students
SELECT roll_no, student_name, department, current_semester
FROM students
WHERE roll_no BETWEEN '25ITR001' AND '25ITR060'
ORDER BY roll_no;
