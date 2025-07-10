package com.college.repository;

import com.college.model.LectureSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureScheduleRepository extends JpaRepository<LectureSchedule, Long> {
}