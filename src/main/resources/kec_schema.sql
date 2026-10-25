CREATE DATABASE IF NOT EXISTS kec_academic;
USE kec_academic;

CREATE TABLE IF NOT EXISTS students (
  roll_no VARCHAR(20) PRIMARY KEY,
  student_name VARCHAR(100) NOT NULL,
  department VARCHAR(30) NOT NULL,
  current_semester INT NOT NULL CHECK (current_semester BETWEEN 1 AND 8)
);
CREATE TABLE IF NOT EXISTS courses (
  course_code VARCHAR(20) PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  credits DECIMAL(4,1) NOT NULL CHECK (credits > 0),
  course_type ENUM('THEORY','PRACTICAL','EMBEDDED') NOT NULL
);
CREATE TABLE IF NOT EXISTS enrollments (
  enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
  roll_no VARCHAR(20) NOT NULL,
  course_code VARCHAR(20) NOT NULL,
  semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
  total_classes INT NOT NULL DEFAULT 0 CHECK (total_classes >= 0),
  classes_attended INT NOT NULL DEFAULT 0 CHECK (classes_attended >= 0),
  medical_condonation BOOLEAN NOT NULL DEFAULT FALSE,
  UNIQUE (roll_no, course_code, semester),
  CHECK (classes_attended <= total_classes),
  FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE,
  FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS assessment_marks (
  enrollment_id INT PRIMARY KEY,
  cia1 DECIMAL(5,2) NULL CHECK (cia1 BETWEEN 0 AND 100),
  cia2 DECIMAL(5,2) NULL CHECK (cia2 BETWEEN 0 AND 100),
  assignment_mark DECIMAL(5,2) NULL CHECK (assignment_mark BETWEEN 0 AND 100),
  ese_mark DECIMAL(5,2) NULL CHECK (ese_mark BETWEEN 0 AND 100),
  ese_absent BOOLEAN NOT NULL DEFAULT FALSE,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
  CHECK (ese_absent = FALSE OR ese_mark IS NULL)
);
