package com.college.controller.home;

import com.college.CourseRegistrationSystemApplication;
import com.college.model.Program;
import com.college.repository.ProgramRepository;
import com.college.repository.UserProfileRepository;
import com.college.repository.UserRepository;
import com.college.controller.home.HomeController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.college.repository.CourseRepository;
import com.college.repository.EnrollmentRepository;
import com.college.repository.LectureScheduleRepository;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.college.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import com.college.service.UserService;
import org.springframework.mock.web.MockMultipartFile;

@Import(SecurityConfig.class)
@WebMvcTest(controllers = HomeController.class)
public class HomeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserProfileRepository userProfileRepository;
    @MockBean
    private CourseRepository courseRepository;
    @MockBean
    private EnrollmentRepository enrollmentRepository;
    @MockBean
    private ProgramRepository programRepository;
    @MockBean
    private LectureScheduleRepository lectureScheduleRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UserService userService;
    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void testRegisterValidation() throws Exception {
        Program program = new Program();
        program.setId(1L);
        program.setName("Computer Science");
        program.setYear("2025");
        when(programRepository.findAllOrderByNameAndYear()).thenReturn(Collections.singletonList(program));
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userProfileRepository.existsByStudentId(any())).thenReturn(false);
        when(programRepository.findById(any())).thenReturn(java.util.Optional.of(program));
        MockMultipartFile emptyFile = new MockMultipartFile("profileImage", "", "application/octet-stream", new byte[0]);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/register")
                .file(emptyFile)
                .param("username", "")
                .param("password", "")
                .param("email", "")
                .param("fullName", "")
                .param("studentId", "")
                .param("programName", "Computer Science")
                .param("year", "2025")
                .with(csrf()))
                .andExpect(MockMvcResultMatchers.model().attributeExists("error"))
                .andExpect(MockMvcResultMatchers.view().name("register"));
    }

    @Test
    void testRegisterSuccess() throws Exception {
        Program program = new Program();
        program.setId(1L);
        program.setName("Computer Science");
        program.setYear("2025");
        when(programRepository.findAllOrderByNameAndYear()).thenReturn(Collections.singletonList(program));
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userProfileRepository.existsByStudentId(any())).thenReturn(false);
        when(programRepository.findById(any())).thenReturn(java.util.Optional.of(program));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userProfileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(authenticationManager.authenticate(any())).thenReturn(Mockito.mock(Authentication.class));
        com.college.model.User testUser = com.college.model.User.builder()
            .id(1L)
            .username("testuser")
            .password("encoded")
            .role("ROLE_USER")
            .email("test@univ.edu")
            .program(program)
            .build();
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(testUser));
        MockMultipartFile emptyFile = new MockMultipartFile("profileImage", "", "application/octet-stream", new byte[0]);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/register")
                .file(emptyFile)
                .param("username", "testuser")
                .param("password", "password1")
                .param("email", "test@univ.edu")
                .param("fullName", "Test User")
                .param("studentId", "123456")
                .param("programName", "Computer Science")
                .param("year", "2025")
                .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/"));
    }
}