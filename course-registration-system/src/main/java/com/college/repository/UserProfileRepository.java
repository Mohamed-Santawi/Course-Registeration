package com.college.repository;

import com.college.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
}