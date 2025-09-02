package com.college.controller;

import com.college.repository.ProgramRepository;
import com.college.model.Program;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.college.repository.CourseRepository;
import com.college.model.Course;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Controller
public class ProgramController {
    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/programs")
    public String getPrograms(Model model) {
        List<Program> programs = programRepository.findAllOrderByNameAndYear();
        model.addAttribute("programs", programs);
        return "programs";
    }

    @GetMapping("/programs/{programId}/courses")
    public String getProgramCourses(@PathVariable Long programId, Model model) {
        Program program = programRepository.findById(programId).orElse(null);
        if (program == null) {
            model.addAttribute("error", "Program not found");
            return "program_courses";
        }
        List<Course> courses = courseRepository.findByProgram(program);
        // Group courses by semester
        Map<String, List<Course>> coursesBySemester = new LinkedHashMap<>();
        for (Course c : courses) {
            coursesBySemester.computeIfAbsent(c.getSemester(), k -> new ArrayList<>()).add(c);
        }

        // DEBUG: Print the structure
        System.out.println("coursesBySemester type: " + coursesBySemester.getClass());
        for (Map.Entry<String, List<Course>> entry : coursesBySemester.entrySet()) {
            System.out.println("Semester: " + entry.getKey() + ", List type: " + entry.getValue().getClass());
            for (Object obj : entry.getValue()) {
                System.out.println("  Course object: " + obj + ", type: " + obj.getClass());
            }
        }

        model.addAttribute("program", program);
        model.addAttribute("coursesBySemester", coursesBySemester);
        return "program_courses";
    }
}