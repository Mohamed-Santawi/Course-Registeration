package com.college.controller.admin;

import com.college.model.Course;
import com.college.model.Program;
import com.college.model.Enrollment;
import com.college.model.User;
import com.college.model.LectureSchedule;
import com.college.repository.CourseRepository;
import com.college.repository.EnrollmentRepository;
import com.college.repository.ProgramRepository;
import com.college.repository.UserRepository;
import com.college.repository.LectureScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final LectureScheduleRepository lectureScheduleRepository;

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        List<Course> courses = courseRepository.findAll();
        List<Program> programs = programRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("programs", programs);
        model.addAttribute("enrollmentCounts", courses.stream().collect(java.util.stream.Collectors.toMap(
                Course::getId,
                c -> c.getEnrollments() != null ? c.getEnrollments().size() : 0
        )));
        return "admin";
    }

    @PostMapping("/admin/course/add")
    public String addCourse(@RequestParam String title,
                            @RequestParam String description,
                            @RequestParam String instructor,
                            @RequestParam Integer capacity,
                            @RequestParam List<Long> programIds) {
        Course course = Course.builder()
                .title(title)
                .description(description)
                .instructor(instructor)
                .capacity(capacity)
                .build();
        List<Program> programs = programRepository.findAllById(programIds);
        course.setPrograms(new java.util.HashSet<>(programs));
        courseRepository.save(course);
        return "redirect:/admin";
    }

    @PostMapping("/admin/course/delete")
    public String deleteCourse(@RequestParam Long courseId) {
        courseRepository.deleteById(courseId);
        return "redirect:/admin";
    }

    @GetMapping("/admin/course/edit/{id}")
    public String editCourse(@PathVariable Long id, Model model) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isPresent()) {
            model.addAttribute("course", courseOpt.get());
            model.addAttribute("programs", programRepository.findAll());
            return "edit_course";
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/course/edit")
    public String updateCourse(@RequestParam Long id,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam String instructor,
                               @RequestParam Integer capacity,
                               @RequestParam List<Long> programIds) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setTitle(title);
            course.setDescription(description);
            course.setInstructor(instructor);
            course.setCapacity(capacity);
            List<Program> programs = programRepository.findAllById(programIds);
            course.setPrograms(new java.util.HashSet<>(programs));
            courseRepository.save(course);
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/enrollments/{courseId}")
    public String viewEnrollments(@PathVariable Long courseId, Model model) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            List<Enrollment> enrollments = course.getEnrollments();
            model.addAttribute("course", course);
            model.addAttribute("enrollments", enrollments);
            return "course_enrollments";
        }
        return "redirect:/admin";
    }

    // PROGRAM MANAGEMENT
    @GetMapping("/admin/programs")
    public String listPrograms(Model model) {
        model.addAttribute("programs", programRepository.findAll());
        return "programs";
    }
    @PostMapping("/admin/programs/add")
    public String addProgram(@RequestParam String name, @RequestParam String year) {
        programRepository.save(Program.builder().name(name).year(year).build());
        return "redirect:/admin/programs";
    }
    @PostMapping("/admin/programs/delete")
    public String deleteProgram(@RequestParam Long programId) {
        programRepository.deleteById(programId);
        return "redirect:/admin/programs";
    }
    // USER MANAGEMENT
    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }
    @PostMapping("/admin/users/delete")
    public String deleteUser(@RequestParam Long userId) {
        userRepository.deleteById(userId);
        return "redirect:/admin/users";
    }
    // SCHEDULE MANAGEMENT
    @GetMapping("/admin/schedule/add/{courseId}")
    public String addScheduleForm(@PathVariable Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "add_schedule";
    }
    @PostMapping("/admin/schedule/add")
    public String addSchedule(@RequestParam Long courseId,
                              @RequestParam String dayOfWeek,
                              @RequestParam String startTime,
                              @RequestParam String endTime,
                              @RequestParam String location) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            LectureSchedule schedule = LectureSchedule.builder()
                .course(course)
                .dayOfWeek(dayOfWeek)
                .startTime(java.time.LocalTime.parse(startTime))
                .endTime(java.time.LocalTime.parse(endTime))
                .location(location)
                .build();
            lectureScheduleRepository.save(schedule);
        }
        return "redirect:/admin";
    }
    @PostMapping("/admin/schedule/delete")
    public String deleteSchedule(@RequestParam Long scheduleId) {
        lectureScheduleRepository.deleteById(scheduleId);
        return "redirect:/admin";
    }
}