package com.college.controller;

import com.college.model.*;
import com.college.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest
class CourseControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private com.college.repository.UserRepository userRepository;
    @MockBean
    private CourseRepository courseRepository;
    @MockBean
    private EnrollmentRepository enrollmentRepository;

    @Test
    @WithMockUser(username = "student1", roles = {"USER"})
    void testCourseListAndEnroll() throws Exception {
        Program program = Program.builder().id(1L).name("CS").year("1st").build();
        com.college.model.User user = com.college.model.User.builder().id(1L).username("student1").program(program).build();
        Course course = Course.builder().id(2L).title("Math").capacity(30).programs(new HashSet<>(List.of(program))).schedules(new ArrayList<>()).enrollments(new ArrayList<>()).build();
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(user));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(enrollmentRepository.findAll()).thenReturn(new ArrayList<>());
        mockMvc.perform(MockMvcRequestBuilders.get("/courses"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeExists("courses"));
        mockMvc.perform(MockMvcRequestBuilders.post("/courses/enroll").param("courseId", "2"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "student1", roles = {"USER"})
    void testAlreadyEnrolled() throws Exception {
        Program program = Program.builder().id(1L).name("CS").year("1st").build();
        com.college.model.User user = com.college.model.User.builder().id(1L).username("student1").program(program).build();
        Course course = Course.builder().id(2L).title("Math").capacity(30).programs(new HashSet<>(List.of(program))).schedules(new ArrayList<>()).enrollments(new ArrayList<>()).build();
        Enrollment enrollment = Enrollment.builder().user(user).course(course).build();
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(user));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));
        mockMvc.perform(MockMvcRequestBuilders.post("/courses/enroll").param("courseId", "2"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "student1", roles = {"USER"})
    void testScheduleConflict() throws Exception {
        Program program = Program.builder().id(1L).name("CS").year("1st").build();
        com.college.model.User user = com.college.model.User.builder().id(1L).username("student1").program(program).build();
        Course course1 = Course.builder().id(2L).title("Math").capacity(30).programs(new HashSet<>(List.of(program))).schedules(new ArrayList<>()).enrollments(new ArrayList<>()).build();
        Course course2 = Course.builder().id(3L).title("Physics").capacity(30).programs(new HashSet<>(List.of(program))).schedules(new ArrayList<>()).enrollments(new ArrayList<>()).build();
        LectureSchedule s1 = LectureSchedule.builder().dayOfWeek("MONDAY").startTime(LocalTime.of(9,0)).endTime(LocalTime.of(11,0)).course(course1).build();
        LectureSchedule s2 = LectureSchedule.builder().dayOfWeek("MONDAY").startTime(LocalTime.of(10,0)).endTime(LocalTime.of(12,0)).course(course2).build();
        course1.setSchedules(List.of(s1));
        course2.setSchedules(List.of(s2));
        Enrollment enrollment = Enrollment.builder().user(user).course(course1).build();
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(user));
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course2));
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));
        mockMvc.perform(MockMvcRequestBuilders.post("/courses/enroll").param("courseId", "3"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }
}