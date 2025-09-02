package com.college.config;

import com.college.model.*;
import com.college.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LectureScheduleRepository lectureScheduleRepository;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            // Programs: create all combinations
            List<String> programNames = List.of("Computer Science", "Electrical Engineering", "Business Administration");
            List<String> years = List.of("1st Year", "2nd Year", "3rd Year");
            List<Program> allPrograms = new ArrayList<>();
            for (String pname : programNames) {
                for (String y : years) {
                    allPrograms.add(programRepository.save(Program.builder().name(pname).year(y).build()));
                }
            }

            // Courses (only insert if not already present)
            Course math = courseRepository.findByCode("MATH101").orElse(null);
            if (math == null) {
                math = Course.builder().title("Mathematics I").description("Intro to Math").instructor("Dr. Smith").capacity(30).code("MATH101").year("1st Year").semester("A").build();
                math.setPrograms(new HashSet<>(Arrays.asList(allPrograms.get(0), allPrograms.get(1)))); // Assuming CS and EE are first two
                math = courseRepository.save(math);
            }
            Course prog = courseRepository.findByCode("CS101").orElse(null);
            if (prog == null) {
                prog = Course.builder().title("Programming I").description("Intro to Programming").instructor("Dr. Alice").capacity(25).code("CS101").year("2nd Year").semester("A").build();
                prog.setPrograms(new HashSet<>(Arrays.asList(allPrograms.get(0)))); // Assuming CS is first
                prog = courseRepository.save(prog);
            }
            Course advAlgo = courseRepository.findByCode("CS501").orElse(null);
            if (advAlgo == null) {
                advAlgo = Course.builder().title("Advanced Algorithms").description("MSc Level").instructor("Dr. Bob").capacity(20).code("CS501").year("3rd Year").semester("B").build();
                advAlgo.setPrograms(new HashSet<>(Arrays.asList(allPrograms.get(2)))); // Assuming BA is third
                advAlgo = courseRepository.save(advAlgo);
            }

            // Assign courses to programs
            allPrograms.get(0).setCourses(new HashSet<>(Arrays.asList(math, prog))); // CS (1st Year, 2nd Year)
            allPrograms.get(1).setCourses(new HashSet<>(Collections.singletonList(math))); // EE (1st Year)
            allPrograms.get(2).setCourses(new HashSet<>(Collections.singletonList(advAlgo))); // BA (3rd Year)
            programRepository.saveAll(allPrograms);

            // Lecture Schedules
            LectureSchedule mathMon = LectureSchedule.builder().course(math).dayOfWeek("MONDAY").startTime(LocalTime.of(9,0)).endTime(LocalTime.of(11,0)).location("Room 101").build();
            LectureSchedule progTue = LectureSchedule.builder().course(prog).dayOfWeek("TUESDAY").startTime(LocalTime.of(10,0)).endTime(LocalTime.of(12,0)).location("Room 102").build();
            LectureSchedule advAlgoWed = LectureSchedule.builder().course(advAlgo).dayOfWeek("WEDNESDAY").startTime(LocalTime.of(14,0)).endTime(LocalTime.of(16,0)).location("Room 201").build();
            lectureScheduleRepository.saveAll(Arrays.asList(mathMon, progTue, advAlgoWed));

            // Users (students and admin)
            User admin = User.builder().username("admin").password("$2a$10$adminpasshash").role("ROLE_ADMIN").email("admin@univ.edu").program(allPrograms.get(2)).build(); // BA
            User student1 = User.builder().username("student1").password("$2a$10$student1hash").role("ROLE_USER").email("stud1@univ.edu").program(allPrograms.get(0)).build(); // CS 1st Year
            User student2 = User.builder().username("student2").password("$2a$10$student2hash").role("ROLE_USER").email("stud2@univ.edu").program(allPrograms.get(1)).build(); // EE 1st Year
            userRepository.saveAll(Arrays.asList(admin, student1, student2));

            // User Profiles
            userProfileRepository.save(UserProfile.builder().user(admin).fullName("Admin User").studentId("A000").profileImage(null).build());
            userProfileRepository.save(UserProfile.builder().user(student1).fullName("Student One").studentId("S001").profileImage(null).build());
            userProfileRepository.save(UserProfile.builder().user(student2).fullName("Student Two").studentId("S002").profileImage(null).build());

            // Enrollments
            enrollmentRepository.save(Enrollment.builder().user(student1).course(math).enrollmentDate(LocalDate.now()).build());
            enrollmentRepository.save(Enrollment.builder().user(student1).course(prog).enrollmentDate(LocalDate.now()).build());
            enrollmentRepository.save(Enrollment.builder().user(student2).course(math).enrollmentDate(LocalDate.now()).build());
            enrollmentRepository.save(Enrollment.builder().user(admin).course(advAlgo).enrollmentDate(LocalDate.now()).build());
        } else {
            System.out.println("Admin user already exists, skipping data initialization.");
        }
    }
}