package com.college.controller;

import com.college.repository.EnrollmentRepository;
import com.college.repository.UserRepository;
import com.college.model.Enrollment;
import com.college.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.college.model.Course;
import com.college.model.LectureSchedule;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

@Controller
public class ScheduleController {
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/lecture-schedule")
    public String getLectureSchedule(@RequestParam(value = "year", required = false) String year,
                         @RequestParam(value = "semester", required = false) String semester,
                         Model model, Principal principal) {
        List<String> years = List.of("1st Year", "2nd Year", "3rd Year");
        List<String> semesters = List.of("A", "B", "C");
        model.addAttribute("years", years);
        model.addAttribute("semesters", semesters);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedSemester", semester);
        if (year == null || semester == null) {
            model.addAttribute("noSelection", true);
            model.addAttribute("courses", List.of());
            model.addAttribute("schedules", List.of());
            model.addAttribute("hasOverlap", false);
            return "schedule";
        }
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            model.addAttribute("noSelection", true);
            model.addAttribute("userError", true);
            model.addAttribute("userErrorMessage", "User not found. Please log in again.");
            model.addAttribute("courses", List.of());
            model.addAttribute("schedules", List.of());
            model.addAttribute("hasOverlap", false);
            return "schedule";
        }
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        List<Course> courses = enrollments.stream()
            .map(Enrollment::getCourse)
            .filter(c -> year.equals(c.getYear()) && semester.equals(c.getSemester()))
            .toList();
        // Fetch lecture schedules for these courses
        List<LectureSchedule> schedules = new ArrayList<>();
        for (Course c : courses) {
            if (c.getSchedules() != null) {
                schedules.addAll(c.getSchedules());
            }
        }
        // Check for overlaps
        boolean hasOverlap = false;
        for (int i = 0; i < schedules.size(); i++) {
            for (int j = i + 1; j < schedules.size(); j++) {
                LectureSchedule s1 = schedules.get(i);
                LectureSchedule s2 = schedules.get(j);
                if (s1.getDayOfWeek().equals(s2.getDayOfWeek())) {
                    if (!(s1.getEndTime().isBefore(s2.getStartTime()) || s2.getEndTime().isBefore(s1.getStartTime()))) {
                        hasOverlap = true;
                        break;
                    }
                }
            }
            if (hasOverlap) break;
        }
        model.addAttribute("courses", courses);
        model.addAttribute("schedules", schedules);
        model.addAttribute("hasOverlap", hasOverlap);
        model.addAttribute("noSelection", false);
        return "schedule";
    }

    @GetMapping("/lecture-schedules")
    public String getAllLectureSchedules(Model model, Principal principal) {
        if (principal == null) {
            model.addAttribute("userError", true);
            model.addAttribute("userErrorMessage", "User not found. Please log in again.");
            model.addAttribute("coursesWithSchedules", List.of());
            model.addAttribute("hasOverlap", false);
            return "lecture_schedules";
        }
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            model.addAttribute("userError", true);
            model.addAttribute("userErrorMessage", "User not found. Please log in again.");
            model.addAttribute("coursesWithSchedules", List.of());
            model.addAttribute("hasOverlap", false);
            return "lecture_schedules";
        }
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        List<Course> courses = enrollments.stream().map(Enrollment::getCourse).toList();
        // Collect all schedules
        List<LectureSchedule> allSchedules = new ArrayList<>();
        for (Course c : courses) {
            if (c.getSchedules() != null) {
                allSchedules.addAll(c.getSchedules());
            }
        }
        // Check for overlaps
        boolean hasOverlap = false;
        for (int i = 0; i < allSchedules.size(); i++) {
            for (int j = i + 1; j < allSchedules.size(); j++) {
                LectureSchedule s1 = allSchedules.get(i);
                LectureSchedule s2 = allSchedules.get(j);
                if (s1.getDayOfWeek().equals(s2.getDayOfWeek())) {
                    if (!(s1.getEndTime().isBefore(s2.getStartTime()) || s2.getEndTime().isBefore(s1.getStartTime()))) {
                        hasOverlap = true;
                        break;
                    }
                }
            }
            if (hasOverlap) break;
        }
        model.addAttribute("coursesWithSchedules", courses);
        model.addAttribute("hasOverlap", hasOverlap);
        return "lecture_schedules";
    }
}