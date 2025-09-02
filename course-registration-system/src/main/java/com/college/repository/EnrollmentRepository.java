package com.college.repository;

import com.college.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.college.model.User;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUser(User user);
}