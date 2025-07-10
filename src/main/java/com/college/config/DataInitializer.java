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
        // Programs
        Program year1 = programRepository.save(Program.builder().name("Computer Science").year("1st Year").build());
        Program year2 = programRepository.save(Program.builder().name("Computer Science").year("2nd Year").build());
        Program msc = programRepository.save(Program.builder().name("Computer Science").year("MSc").build());

        // Courses
        Course math = Course.builder().title("Mathematics I").description("Intro to Math").instructor("Dr. Smith").capacity(30).build();
        Course prog = Course.builder().title("Programming I").description("Intro to Programming").instructor("Dr. Alice").capacity(25).build();
        Course advAlgo = Course.builder().title("Advanced Algorithms").description("MSc Level").instructor("Dr. Bob").capacity(20).build();

        // Assign courses to programs
        math.setPrograms(new HashSet<>(Arrays.asList(year1, year2)));
        prog.setPrograms(new HashSet<>(Arrays.asList(year1)));
        advAlgo.setPrograms(new HashSet<>(Arrays.asList(msc)));

        math = courseRepository.save(math);
        prog = courseRepository.save(prog);
        advAlgo = courseRepository.save(advAlgo);

        year1.setCourses(new HashSet<>(Arrays.asList(math, prog)));
        year2.setCourses(new HashSet<>(Collections.singletonList(math)));
        msc.setCourses(new HashSet<>(Collections.singletonList(advAlgo)));
        programRepository.saveAll(Arrays.asList(year1, year2, msc));

        // Lecture Schedules
        LectureSchedule mathMon = LectureSchedule.builder().course(math).dayOfWeek("MONDAY").startTime(LocalTime.of(9,0)).endTime(LocalTime.of(11,0)).location("Room 101").build();
        LectureSchedule progTue = LectureSchedule.builder().course(prog).dayOfWeek("TUESDAY").startTime(LocalTime.of(10,0)).endTime(LocalTime.of(12,0)).location("Room 102").build();
        LectureSchedule advAlgoWed = LectureSchedule.builder().course(advAlgo).dayOfWeek("WEDNESDAY").startTime(LocalTime.of(14,0)).endTime(LocalTime.of(16,0)).location("Room 201").build();
        lectureScheduleRepository.saveAll(Arrays.asList(mathMon, progTue, advAlgoWed));

        // Users (students and admin)
        User admin = User.builder().username("admin").password("$2a$10$adminpasshash").role("ROLE_ADMIN").email("admin@univ.edu").program(msc).build();
        User student1 = User.builder().username("student1").password("$2a$10$student1hash").role("ROLE_USER").email("stud1@univ.edu").program(year1).build();
        User student2 = User.builder().username("student2").password("$2a$10$student2hash").role("ROLE_USER").email("stud2@univ.edu").program(year2).build();
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
    }
}