package com.college.controller.course;

import com.college.model.*;
import com.college.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final ProgramRepository programRepository;

    @GetMapping("/courses")
    public String getCourses(@RequestParam(value = "programName", required = false) String programName,
                        @RequestParam(value = "year", required = false) String year,
                        @RequestParam(value = "semester", required = false) String semester,
                        Model model) {
        List<String> programNames = List.of("Computer Science", "Electrical Engineering", "Business Administration");
        List<String> years = List.of("1st Year", "2nd Year", "3rd Year");
        List<String> semesters = List.of("A", "B", "C");
        List<Course> courses = new ArrayList<>();
        if (programName != null && year != null && semester != null) {
            courses = courseRepository.findByProgramNameAndYearAndSemester(programName, year, semester);
            // Pad to at least 3 courses with placeholders
            int minCourses = 3;
            while (courses.size() < minCourses) {
                Course placeholder = new Course();
                placeholder.setTitle("No course available for this slot");
                placeholder.setCode("");
                courses.add(placeholder);
            }
        }
        model.addAttribute("programNames", programNames);
        model.addAttribute("years", years);
        model.addAttribute("semesters", semesters);
        model.addAttribute("selectedProgramName", programName);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedSemester", semester);
        model.addAttribute("courses", courses);
        model.addAttribute("lectureScheduleLink", "/lecture-schedule");
        return "courses";
    }

    @GetMapping("/courses/list")
    public String fullCourseList(@RequestParam(value = "year", required = false) String selectedYear, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getProgram() == null) {
            model.addAttribute("groupedCourses", new LinkedHashMap<>());
            model.addAttribute("years", List.of("A", "B", "C"));
            model.addAttribute("selectedYear", selectedYear);
            return "courses_list";
        }
        // If no year selected, default to user's year
        String year = selectedYear != null ? selectedYear : user.getProgram().getYear();
        // Fetch all courses for user's program and year (including past years)
        List<Course> courses = courseRepository.findByProgram(user.getProgram());
        // Group by year and type
        Map<String, Map<String, List<Course>>> groupedCourses = new LinkedHashMap<>();
        for (Course c : courses) {
            // Only show courses for selected year or earlier
            if (c.getYear() == null || c.getYear().compareTo(year) > 0) continue;
            groupedCourses.computeIfAbsent(c.getYear(), k -> new LinkedHashMap<>())
                .computeIfAbsent(c.getType() != null ? c.getType() : "Other", k -> new ArrayList<>())
                .add(c);
        }
        // Sort years A, B, C
        List<String> years = List.of("A", "B", "C");
        Map<String, Map<String, List<Course>>> sortedGrouped = new LinkedHashMap<>();
        for (String y : years) {
            if (groupedCourses.containsKey(y)) sortedGrouped.put(y, groupedCourses.get(y));
        }
        model.addAttribute("groupedCourses", sortedGrouped);
        model.addAttribute("years", years);
        model.addAttribute("selectedYear", year);
        return "courses_list";
    }

    @GetMapping("/courses/{id}")
    public String getCourseDetails(@PathVariable Long id, Model model, Principal principal) {
        Course course = courseRepository.findById(id).orElseThrow();
        model.addAttribute("course", course);
        boolean isEligibleToEnroll = false;
        boolean hasScheduleConflict = false;
        boolean alreadyEnrolled = false;
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null && user.getProgram() != null && course.getPrograms() != null && !course.getPrograms().isEmpty()) {
                boolean programMatch = course.getPrograms().stream().anyMatch(p -> p.getId().equals(user.getProgram().getId()));
                boolean yearMatch = course.getYear() != null && course.getYear().equals(user.getProgram().getYear());
                isEligibleToEnroll = programMatch && yearMatch;
                // Check for schedule conflict
                List<Enrollment> currentEnrollments = enrollmentRepository.findByUser(user);
                outer: for (Enrollment enrollment : currentEnrollments) {
                    for (LectureSchedule existingSchedule : enrollment.getCourse().getSchedules()) {
                        for (LectureSchedule newSchedule : course.getSchedules()) {
                            if (existingSchedule.getDayOfWeek().equals(newSchedule.getDayOfWeek())) {
                                if (hasTimeConflict(existingSchedule, newSchedule)) {
                                    hasScheduleConflict = true;
                                    break outer;
                                }
                            }
                        }
                    }
                }
                alreadyEnrolled = currentEnrollments.stream().anyMatch(e -> e.getCourse().getId().equals(course.getId()));
            }
        }
        model.addAttribute("isEligibleToEnroll", isEligibleToEnroll);
        model.addAttribute("hasScheduleConflict", hasScheduleConflict);
        model.addAttribute("alreadyEnrolled", alreadyEnrolled);
        long enrolledCount = enrollmentRepository.findAll().stream()
            .filter(e -> e.getCourse().getId().equals(course.getId()))
            .count();
        model.addAttribute("capacity", course.getCapacity());
        model.addAttribute("seatsLeft", java.util.Objects.nonNull(course.getCapacity()) ? course.getCapacity() - enrolledCount : null);
        return "course_details";
    }

    @PostMapping("/api/courses/unenroll")
    @ResponseBody
    public Map<String, Object> unenroll(@RequestParam Long enrollmentId) {
        Map<String, Object> response = new HashMap<>();
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOpt.isPresent()) {
            enrollmentRepository.deleteById(enrollmentId);
            response.put("success", true);
            response.put("message", "Successfully unenrolled from course.");
        } else {
            response.put("success", false);
            response.put("error", "Enrollment not found.");
        }
        return response;
    }

    @PostMapping("/api/courses/enroll")
    @ResponseBody
    public Map<String, Object> enroll(@RequestParam Long courseId) {
        Map<String, Object> response = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            response.put("success", false);
            response.put("error", "User not found");
            return response;
        }

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            response.put("success", false);
            response.put("error", "Course not found");
            return response;
        }

        // Check for program and year eligibility (semester is not restricted)
        if (user.getProgram() == null || course.getPrograms() == null || course.getPrograms().isEmpty()) {
            response.put("success", false);
            response.put("error", "Your program information or course program is missing. Please contact administration.");
            return response;
        }
        boolean programMatch = course.getPrograms().stream().anyMatch(p -> p.getId().equals(user.getProgram().getId()));
        boolean yearMatch = course.getYear() != null && course.getYear().equals(user.getProgram().getYear());
        if (!programMatch || !yearMatch) {
            response.put("success", false);
            response.put("error", "You are not eligible to enroll in this course. Only courses for your registered program and year are allowed.");
            return response;
        }

        // Check for schedule conflicts
        List<Enrollment> currentEnrollments = enrollmentRepository.findAll().stream()
            .filter(e -> e.getUser().getId().equals(user.getId()))
            .toList();

        for (Enrollment enrollment : currentEnrollments) {
            for (LectureSchedule existingSchedule : enrollment.getCourse().getSchedules()) {
                for (LectureSchedule newSchedule : course.getSchedules()) {
                    if (existingSchedule.getDayOfWeek().equals(newSchedule.getDayOfWeek())) {
                        // Check for time overlap
                        if (hasTimeConflict(existingSchedule, newSchedule)) {
                            response.put("success", false);
                            response.put("error", "Schedule conflict detected with '" + enrollment.getCourse().getTitle() + "' on " + existingSchedule.getDayOfWeek() + " (" + existingSchedule.getStartTime() + " - " + existingSchedule.getEndTime() + ")");
                            return response;
                        }
                    }
                }
            }
        }

        // Check if already enrolled
        boolean alreadyEnrolled = currentEnrollments.stream().anyMatch(e -> e.getCourse().getId().equals(courseId));
        if (alreadyEnrolled) {
            response.put("success", false);
            response.put("error", "You are already enrolled in this course.");
            return response;
        }

        // Check course capacity
        long enrolledCount = enrollmentRepository.findAll().stream()
            .filter(e -> e.getCourse().getId().equals(courseId))
            .count();
        if (java.util.Objects.nonNull(course.getCapacity()) && enrolledCount >= course.getCapacity()) {
            response.put("success", false);
            response.put("error", "This course is full. No more seats available.");
            return response;
        }

        // Create enrollment if no conflicts
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollmentRepository.save(enrollment);

        response.put("success", true);
        response.put("message", "Successfully enrolled in " + course.getTitle());
        return response;
    }

    @GetMapping("/enrollments")
    public String getMyEnrollments(Model model, Principal principal) {
        if (principal == null) {
            model.addAttribute("enrollments", List.of());
            return "enrollments";
        }
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            model.addAttribute("enrollments", List.of());
            return "enrollments";
        }
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        model.addAttribute("enrollments", enrollments);
        return "enrollments";
    }

    private boolean hasTimeConflict(LectureSchedule schedule1, LectureSchedule schedule2) {
        // Simple time conflict detection - can be enhanced
        return schedule1.getStartTime().equals(schedule2.getStartTime()) ||
               schedule1.getEndTime().equals(schedule2.getEndTime()) ||
               (schedule1.getStartTime().isBefore(schedule2.getEndTime()) &&
                schedule1.getEndTime().isAfter(schedule2.getStartTime()));
    }
}