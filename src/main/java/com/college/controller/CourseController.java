package com.college.controller;

import com.college.model.*;
import com.college.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CourseController {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/courses")
    public String courses(Model model, Principal principal) {
        if (principal == null) {
            model.addAttribute("courses", Collections.emptyList());
            model.addAttribute("enrollments", Collections.emptyList());
            model.addAttribute("idsForEnrollments", Collections.emptyList());
            return "courses";
        }
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null || user.getProgram() == null) {
            model.addAttribute("courses", Collections.emptyList());
            model.addAttribute("enrollments", Collections.emptyList());
            model.addAttribute("idsForEnrollments", Collections.emptyList());
            return "courses";
        }
        // Get all courses for user's program and past years
        Set<Course> availableCourses = new HashSet<>(user.getProgram().getCourses());
        // Add past years' courses (simulate by adding all courses from all programs with year <= user's year)
        List<Program> allPrograms = user.getProgram().getUsers().stream()
                .map(User::getProgram)
                .distinct()
                .collect(Collectors.toList());
        for (Program p : allPrograms) {
            availableCourses.addAll(p.getCourses());
        }
        // Get user's enrollments
        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
        List<Long> idsForEnrollments = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toList());
        model.addAttribute("courses", availableCourses);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("idsForEnrollments", idsForEnrollments);
        return "courses";
    }

    @PostMapping("/courses/enroll")
    public String enroll(@RequestParam Long courseId, Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("error", "You must be logged in to enroll.");
            return "redirect:/courses";
        }
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found.");
            return "redirect:/courses";
        }
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            model.addAttribute("error", "Course not found.");
            return "redirect:/courses";
        }
        Course course = courseOpt.get();
        // Check if already enrolled
        boolean alreadyEnrolled = enrollmentRepository.findAll().stream()
                .anyMatch(e -> e.getUser().getId().equals(user.getId()) && e.getCourse().getId().equals(courseId));
        if (alreadyEnrolled) {
            model.addAttribute("error", "You are already enrolled in this course.");
            return "redirect:/courses";
        }
        // Check for schedule overlap
        List<Enrollment> userEnrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
        boolean overlap = false;
        for (Enrollment e : userEnrollments) {
            for (LectureSchedule s1 : e.getCourse().getSchedules()) {
                for (LectureSchedule s2 : course.getSchedules()) {
                    if (s1.getDayOfWeek().equals(s2.getDayOfWeek()) &&
                        s1.getStartTime().isBefore(s2.getEndTime()) &&
                        s2.getStartTime().isBefore(s1.getEndTime())) {
                        overlap = true;
                        break;
                    }
                }
                if (overlap) break;
            }
            if (overlap) break;
        }
        if (overlap) {
            model.addAttribute("error", "Schedule conflict detected with your current courses.");
            return "redirect:/courses";
        }
        // Enroll
        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .enrollmentDate(LocalDate.now())
                .build();
        enrollmentRepository.save(enrollment);
        model.addAttribute("success", "Enrolled successfully!");
        return "redirect:/courses";
    }

    // REST endpoint for SPA-style enrollment
    @PostMapping("/api/courses/enroll")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enrollApi(@RequestParam Long courseId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal == null) {
            response.put("success", false);
            response.put("error", "You must be logged in to enroll.");
            return ResponseEntity.badRequest().body(response);
        }
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("error", "User not found.");
            return ResponseEntity.badRequest().body(response);
        }
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            response.put("success", false);
            response.put("error", "Course not found.");
            return ResponseEntity.badRequest().body(response);
        }
        Course course = courseOpt.get();
        boolean alreadyEnrolled = enrollmentRepository.findAll().stream()
                .anyMatch(e -> e.getUser().getId().equals(user.getId()) && e.getCourse().getId().equals(courseId));
        if (alreadyEnrolled) {
            response.put("success", false);
            response.put("error", "You are already enrolled in this course.");
            return ResponseEntity.ok(response);
        }
        // Check for schedule overlap
        List<Enrollment> userEnrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
        boolean overlap = false;
        for (Enrollment e : userEnrollments) {
            for (LectureSchedule s1 : e.getCourse().getSchedules()) {
                for (LectureSchedule s2 : course.getSchedules()) {
                    if (s1.getDayOfWeek().equals(s2.getDayOfWeek()) &&
                        s1.getStartTime().isBefore(s2.getEndTime()) &&
                        s2.getStartTime().isBefore(s1.getEndTime())) {
                        overlap = true;
                        break;
                    }
                }
                if (overlap) break;
            }
            if (overlap) break;
        }
        if (overlap) {
            response.put("success", false);
            response.put("error", "Schedule conflict detected with your current courses.");
            return ResponseEntity.ok(response);
        }
        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .enrollmentDate(LocalDate.now())
                .build();
        enrollmentRepository.save(enrollment);
        response.put("success", true);
        response.put("message", "Enrolled successfully!");
        return ResponseEntity.ok(response);
    }
}