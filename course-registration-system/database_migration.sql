-- Database Migration Script for Course Registration System (Adjusted to match ex4.sql)
-- Drops tables if they exist, creates tables with correct schema, and adds constraints

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- Drop tables if they exist (in correct order for FK constraints)
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS lecture_schedules;
DROP TABLE IF EXISTS course_programs;
DROP TABLE IF EXISTS program_courses;
DROP TABLE IF EXISTS user_profiles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS programs;
DROP TABLE IF EXISTS courses;

-- Create courses table
CREATE TABLE courses (
  id bigint(20) NOT NULL,
  capacity int(11) NOT NULL,
  description varchar(255) DEFAULT NULL,
  instructor varchar(255) DEFAULT NULL,
  title varchar(255) NOT NULL,
  type varchar(255) DEFAULT NULL,
  year varchar(255) DEFAULT NULL,
  semester varchar(255) NOT NULL,
  code varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create programs table
CREATE TABLE programs (
  id bigint(20) NOT NULL,
  name varchar(255) NOT NULL,
  year varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create users table
CREATE TABLE users (
  id bigint(20) NOT NULL,
  email varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  role varchar(255) NOT NULL,
  username varchar(255) NOT NULL,
  program_id bigint(20) DEFAULT NULL,
  CONSTRAINT unique_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create user_profiles table
CREATE TABLE user_profiles (
  id bigint(20) NOT NULL,
  full_name varchar(255) DEFAULT NULL,
  profile_image longblob DEFAULT NULL,
  student_id varchar(255) DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL,
  image_content_type varchar(255) DEFAULT NULL,
  CONSTRAINT unique_student_id UNIQUE (student_id),
  CONSTRAINT FK_user_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create course_programs table (join table)
CREATE TABLE course_programs (
  course_id bigint(20) NOT NULL,
  program_id bigint(20) NOT NULL,
  PRIMARY KEY (course_id, program_id),
  CONSTRAINT fk_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
  CONSTRAINT fk_program FOREIGN KEY (program_id) REFERENCES programs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create enrollments table
CREATE TABLE enrollments (
  id bigint(20) NOT NULL,
  enrollment_date date DEFAULT NULL,
  course_id bigint(20) DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create lecture_schedules table
CREATE TABLE lecture_schedules (
  id bigint(20) NOT NULL,
  day_of_week varchar(255) DEFAULT NULL,
  end_time time(6) DEFAULT NULL,
  location varchar(255) DEFAULT NULL,
  start_time time(6) DEFAULT NULL,
  course_id bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Add any additional constraints or indexes as needed
-- (You can add ALTER TABLE statements here if you want to add FKs to enrollments, etc.)

COMMIT;